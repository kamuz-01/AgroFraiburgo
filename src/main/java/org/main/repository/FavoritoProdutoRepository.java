package org.main.repository;

import java.util.List;

import org.main.models.FavoritoProduto;
import org.main.models.FavoritoProdutoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritoProdutoRepository extends JpaRepository<FavoritoProduto, FavoritoProdutoId> {

    @Query("select f.id.idProduto from FavoritoProduto f where f.id.idUsuario = :idUsuario")
    List<Integer> findIdsProdutosFavoritadosPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("select count(f) from FavoritoProduto f where f.id.idProduto = :idProduto")
    long countFavoritosPorProduto(@Param("idProduto") Integer idProduto);
}
