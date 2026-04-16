package org.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import org.main.models.Produto;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findTop4ByOrderByDataCriacaoDesc();

    @EntityGraph(attributePaths = "produtor")
    List<Produto> findTop4ByProdutor_IdProdutorOrderByDataCriacaoDesc(Integer idProdutor);

    @EntityGraph(attributePaths = "produtor")
    List<Produto> findByProdutorIdProdutor(Integer idProdutor);

    @EntityGraph(attributePaths = "produtor")
    List<Produto> findAllByProdutor_IdProdutor(Integer idProdutor);

    @Query("""
        SELECT p
        FROM Produto p
        WHERE (
            :termo IS NULL
            OR LOWER(COALESCE(p.nomeProduto, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR LOWER(COALESCE(p.descricao, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
        )
          AND (:minPreco IS NULL OR p.preco >= :minPreco)
          AND (:maxPreco IS NULL OR p.preco <= :maxPreco)
        ORDER BY p.dataCriacao DESC
        """)
    List<Produto> buscarPorTermoEPreco(@Param("termo") String termo,
                                       @Param("minPreco") Double minPreco,
                                       @Param("maxPreco") Double maxPreco);

    @EntityGraph(attributePaths = "produtor")
    Optional<Produto> findByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);

    @Transactional
    Long deleteByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);
}