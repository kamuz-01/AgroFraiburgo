package org.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import org.main.models.Produto;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    List<Produto> findByProdutorIdProdutor(Integer idProdutor);

    List<Produto> findAllByProdutor_IdProdutor(Integer idProdutor);

    Optional<Produto> findByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);

    @Transactional
    Long deleteByIdProdutoAndProdutor_IdProdutor(Integer idProduto, Integer idProdutor);
}