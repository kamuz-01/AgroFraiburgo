package org.main.repository;

import java.util.List;

import org.main.models.FavoritoProdutor;
import org.main.models.FavoritoProdutorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritoProdutorRepository extends JpaRepository<FavoritoProdutor, FavoritoProdutorId> {

    @Query("select f.id.idProdutor from FavoritoProdutor f where f.id.idUsuario = :idUsuario")
    List<Integer> findIdsProdutoresFavoritadosPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("select count(f) from FavoritoProdutor f where f.id.idProdutor = :idProdutor")
    long countFavoritosPorProdutor(@Param("idProdutor") Integer idProdutor);
}
