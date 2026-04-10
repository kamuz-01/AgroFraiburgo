package org.main.services;

import java.util.Objects;

import org.main.models.Avaliacao;
import org.main.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
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
        avaliacao.setComentario(comentario);

        return avaliacaoRepository.save(avaliacao);
    }
}
