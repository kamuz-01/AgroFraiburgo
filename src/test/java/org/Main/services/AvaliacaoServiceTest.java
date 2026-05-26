package org.Main.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.models.Avaliacao;
import org.main.models.Produtor;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.ProdutorRepository;
import org.main.services.AvaliacaoService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvaliacaoServiceTest {

    private AvaliacaoRepository avaliacaoRepository;
    private ProdutorRepository produtorRepository;
    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void setUp() {
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        produtorRepository = mock(ProdutorRepository.class);
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, produtorRepository);
    }

    @Test
    void criarOuAtualizarDeveRejeitarComentarioComMaisDe255Caracteres() {
        when(avaliacaoRepository.findByIdConsumidorAndIdProdutor(1, 2)).thenReturn(Optional.of(new Avaliacao()));

        assertThatThrownBy(() -> avaliacaoService.criarOuAtualizar(1, 2, 5, "a".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O comentário da avaliação deve ter no máximo 255 caracteres.");

        verify(avaliacaoRepository, never()).save(org.mockito.ArgumentMatchers.any(Avaliacao.class));
    }

    @Test
    void criarOuAtualizarDeveNormalizarComentarioAntesDeSalvar() {
        Avaliacao avaliacao = new Avaliacao();
        Produtor produtor = new Produtor();
        when(avaliacaoRepository.findByIdConsumidorAndIdProdutor(1, 2)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(avaliacao)).thenReturn(avaliacao);
        when(produtorRepository.findById(2)).thenReturn(Optional.of(produtor));
        when(avaliacaoRepository.contarConsumidoresDistintosPorProdutor(2)).thenReturn(1L);

        Avaliacao salva = avaliacaoService.criarOuAtualizar(1, 2, 5, "  Ótimo atendimento  ");

        assertThat(salva.getComentario()).isEqualTo("Ótimo atendimento");
        verify(avaliacaoRepository).save(avaliacao);
    }
}
