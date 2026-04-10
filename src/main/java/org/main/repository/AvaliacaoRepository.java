package org.main.repository;

import java.util.Optional;

import org.main.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    Optional<Avaliacao> findByIdConsumidorAndIdProdutor(Integer idConsumidor, Integer idProdutor);

    long countByIdProdutor(Integer idProdutor);

    @Query("select avg(a.nota) from Avaliacao a where a.idProdutor = :idProdutor")
    Double buscarMediaPorProdutor(@Param("idProdutor") Integer idProdutor);
}
