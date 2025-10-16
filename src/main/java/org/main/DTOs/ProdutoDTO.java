package org.main.DTOs;

public record ProdutoDTO(Integer idProduto, String nomeProduto, String descricao,
        Double preco, String unidadeMedida, Double quantidadeEstoque,
        String statusProduto, String imagemProduto, String nomeProdutor) 
{}