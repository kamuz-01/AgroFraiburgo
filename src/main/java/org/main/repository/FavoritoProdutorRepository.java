package org.main.repository;

import java.util.List;

import org.main.models.FavoritoProdutor;
import org.main.models.FavoritoProdutorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritoProdutorRepository extends JpaRepository<FavoritoProdutor, FavoritoProdutorId> {

    interface ProdutorFavoritoResumo {
        Integer getIdProdutor();
        Long getTotalFavoritos();
    }

    @Query("select f.id.idProdutor from FavoritoProdutor f where f.id.idUsuario = :idUsuario")
    List<Integer> findIdsProdutoresFavoritadosPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("select count(f) from FavoritoProdutor f where f.id.idProdutor = :idProdutor")
    long countFavoritosPorProdutor(@Param("idProdutor") Integer idProdutor);

    @Query("""
        select f.id.idProdutor as idProdutor,
               count(f) as totalFavoritos
        from FavoritoProdutor f
        where f.id.idProdutor in :idsProdutores
        group by f.id.idProdutor
        """)
    List<ProdutorFavoritoResumo> contarFavoritosPorProdutores(@Param("idsProdutores") List<Integer> idsProdutores);
}
