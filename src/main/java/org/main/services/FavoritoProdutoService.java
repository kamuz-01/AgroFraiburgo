package org.main.services;

import org.main.exceptions.ProdutoNaoEncontradoException;
import org.main.models.FavoritoProduto;
import org.main.models.FavoritoProdutoId;
import org.main.neo4j.Neo4jInteracaoService;
import org.main.repository.FavoritoProdutoRepository;
import org.main.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoritoProdutoService {

    private final FavoritoProdutoRepository favoritoProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final Neo4jInteracaoService neo4jInteracaoService;

    public FavoritoProdutoService(FavoritoProdutoRepository favoritoProdutoRepository,
                                 ProdutoRepository produtoRepository,
                                 Neo4jInteracaoService neo4jInteracaoService) {
        this.favoritoProdutoRepository = favoritoProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.neo4jInteracaoService = neo4jInteracaoService;
    }

    @Transactional
    public ToggleResultado toggle(Integer idUsuario, Integer idProduto) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("Usuário inválido");
        }
        if (idProduto == null) {
            throw new IllegalArgumentException("Produto inválido");
        }

        if (!produtoRepository.existsById(idProduto)) {
            throw new ProdutoNaoEncontradoException();
        }

        FavoritoProdutoId id = new FavoritoProdutoId(idUsuario, idProduto);
        boolean exists = favoritoProdutoRepository.existsById(id);
        if (exists) {
            favoritoProdutoRepository.deleteById(id);
            neo4jInteracaoService.atualizarFavorito(idUsuario, idProduto, false);
            return new ToggleResultado(false);
        }

        favoritoProdutoRepository.save(new FavoritoProduto(idUsuario, idProduto));
        neo4jInteracaoService.atualizarFavorito(idUsuario, idProduto, true);
        return new ToggleResultado(true);
    }

    public record ToggleResultado(boolean favoritado) {}
}
