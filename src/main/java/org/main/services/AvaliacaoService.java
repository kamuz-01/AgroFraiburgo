package org.main.services;

import java.util.Objects;

import org.main.models.Avaliacao;
import org.main.repository.AvaliacaoRepository;
import org.main.repository.ProdutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private static final int COMENTARIO_MAX_CARACTERES = 255;

    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutorRepository produtorRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            ProdutorRepository produtorRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.produtorRepository = produtorRepository;
    }

    @Transactional
    public Avaliacao criarOuAtualizar(Integer idConsumidor, Integer idProdutor, Integer nota, String comentario) {
        if (idConsumidor == null) {
            throw new IllegalArgumentException("Consumidor inválido");
        }
        if (idProdutor == null) {
            throw new IllegalArgumentException("Produtor inválido");
        }
        if (Objects.equals(idConsumidor, idProdutor)) {
            throw new IllegalArgumentException("Você não pode avaliar a si mesmo");
        }
        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota deve ser entre 1 e 5");
        }

        Avaliacao avaliacao = avaliacaoRepository
                .findByIdConsumidorAndIdProdutor(idConsumidor, idProdutor)
                .orElseGet(Avaliacao::new);

        avaliacao.setIdConsumidor(idConsumidor);
        avaliacao.setIdProdutor(idProdutor);
        avaliacao.setNota(nota);
        avaliacao.setComentario(normalizarComentario(comentario));

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        sincronizarAvaliacoesRecebidas(idProdutor);

        return salva;
    }

    private void sincronizarAvaliacoesRecebidas(Integer idProdutor) {
        produtorRepository.findById(idProdutor).ifPresent(produtor -> {
            long totalAvaliacoes = avaliacaoRepository.contarConsumidoresDistintosPorProdutor(idProdutor);
            produtor.setAvaliacoesRecebidas((int) totalAvaliacoes);
            produtorRepository.save(produtor);
        });
    }

    private String normalizarComentario(String comentario) {
        if (comentario == null) {
            return null;
        }

        String comentarioNormalizado = comentario.trim();
        if (comentarioNormalizado.isEmpty()) {
            return null;
        }

        if (comentarioNormalizado.length() > COMENTARIO_MAX_CARACTERES) {
            throw new IllegalArgumentException("O comentário da avaliação deve ter no máximo 255 caracteres.");
        }

        return comentarioNormalizado;
    }
}
