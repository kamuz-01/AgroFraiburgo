package org.Main.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.enums.StatusProduto;
import org.main.models.Produto;
import org.main.repository.ProdutoRepository;
import org.main.services.ProdutoService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoServiceTest {

    private ProdutoRepository produtoRepository;
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoRepository = mock(ProdutoRepository.class);
        produtoService = new ProdutoService(produtoRepository);
    }

    @Test
    void atualizarProdutoDeveRejeitarDescricaoComMaisDe255Caracteres() {
        Produto produto = produtoExistente();
        when(produtoRepository.findByIdProdutoAndProdutor_IdProdutor(1, 9)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> produtoService.atualizarProduto(
                1, 9, "Morango", "a".repeat(256), 12.0, "kg", 5.0, "COM_ESTOQUE", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A descrição do produto deve ter no máximo 255 caracteres.");

        verify(produtoRepository, never()).save(produto);
    }

    @Test
    void atualizarProdutoDeveNormalizarDescricaoAntesDeSalvar() throws Exception {
        Produto produto = produtoExistente();
        when(produtoRepository.findByIdProdutoAndProdutor_IdProdutor(1, 9)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto atualizado = produtoService.atualizarProduto(
                1, 9, "Morango", "  Fresco e orgânico  ", 12.0, "kg", 5.0, "COM_ESTOQUE", null);

        assertThat(atualizado.getDescricao()).isEqualTo("Fresco e orgânico");
        verify(produtoRepository).save(produto);
    }

    private Produto produtoExistente() {
        Produto produto = new Produto();
        produto.setIdProduto(1);
        produto.setStatusProduto(StatusProduto.COM_ESTOQUE);
        return produto;
    }
}
