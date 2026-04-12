package org.main.startup;

import org.main.repository.AvaliacaoRepository;
import org.main.repository.ProdutorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class ProdutorAvaliacoesRecebidasSyncRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdutorAvaliacoesRecebidasSyncRunner.class);

    private final ProdutorRepository produtorRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public ProdutorAvaliacoesRecebidasSyncRunner(ProdutorRepository produtorRepository,
                                                 AvaliacaoRepository avaliacaoRepository) {
        this.produtorRepository = produtorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            produtorRepository.findAll().forEach(produtor -> {
                if (produtor.getIdProdutor() == null) {
                    return;
                }

                long totalAvaliacoes = avaliacaoRepository.contarConsumidoresDistintosPorProdutor(produtor.getIdProdutor());
                Integer novoTotal = Math.toIntExact(totalAvaliacoes);
                if (!Integer.valueOf(novoTotal).equals(produtor.getAvaliacoesRecebidas())) {
                    produtor.setAvaliacoesRecebidas(novoTotal);
                    produtorRepository.save(produtor);
                }
            });
        } catch (DataAccessException ex) {
            log.debug("Sync de avaliacoes_recebidas ignorado na inicialização: {}", ex.getMessage());
        }
    }
}