package org.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Produto> findTop4ByProdutor_IdProdutorOrderByDataCriacaoDesc(Integer idProdutor);

    List<Produto> findByProdutorIdProdutor(Integer idProdutor);

    List<Produto> findAllByProdutor_IdProdutor(Integer idProdutor);

    @Query("""
        SELECT p
        FROM Produto p
        WHERE LOWER(COALESCE(p.nomeProduto, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
           OR LOWER(COALESCE(p.descricao, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
        ORDER BY p.dataCriacao DESC
        """)
    List<Produto> buscarPorTermo(@Param("termo") String termo);

    Optional<Produto> findByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);

    @Transactional
    Long deleteByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);
}