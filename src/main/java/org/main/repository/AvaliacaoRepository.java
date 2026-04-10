package org.main.repository;

import java.util.List;
import java.util.Optional;

import org.main.models.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    interface ProdutorRatingResumo {
        Integer getIdProdutor();
        Double getMedia();
        Long getTotalUsuarios();
    }

    Optional<Avaliacao> findByIdConsumidorAndIdProdutor(Integer idConsumidor, Integer idProdutor);

    List<Avaliacao> findTop10ByIdProdutorOrderByDataAvaliacaoDesc(Integer idProdutor);

    long countByIdProdutor(Integer idProdutor);

    @Query("select count(distinct a.idConsumidor) from Avaliacao a where a.idProdutor = :idProdutor")
    long contarConsumidoresDistintosPorProdutor(@Param("idProdutor") Integer idProdutor);

    @Query("select avg(a.nota) from Avaliacao a where a.idProdutor = :idProdutor")
    Double buscarMediaPorProdutor(@Param("idProdutor") Integer idProdutor);

    @Query("select a.idProdutor as idProdutor, avg(a.nota) as media, count(distinct a.idConsumidor) as totalUsuarios " +
           "from Avaliacao a group by a.idProdutor order by avg(a.nota) desc, count(distinct a.idConsumidor) desc")
    List<ProdutorRatingResumo> listarTopProdutores(Pageable pageable);
}
