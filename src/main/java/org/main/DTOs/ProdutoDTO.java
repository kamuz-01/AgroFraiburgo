package org.main.DTOs;

import org.main.models.Produto;

public record ProdutoDTO(Integer idProduto, String nomeProduto, String descricao,
                Double preco, String unidadeMedida, Double quantidadeEstoque,
                String statusProduto, String imagemProduto, String nomeProdutor) {

        public static ProdutoDTO fromEntity(Produto produto) {
                if (produto == null) {
                        return null;
                }

                String nomeProdutor = null;
                var produtor = produto.getProdutor();
                if (produtor != null && produtor.getIdProdutor() != null) {
                        nomeProdutor = "Produtor #" + produtor.getIdProdutor();
                }

                return new ProdutoDTO(
                                produto.getIdProduto(),
                                produto.getNomeProduto(),
                                produto.getDescricao(),
                                produto.getPreco(),
                                produto.getUnidadeMedida(),
                                produto.getQuantidadeEstoque(),
                                produto.getStatusProduto() != null ? produto.getStatusProduto().name() : null,
                                produto.getImagemProduto(),
                                nomeProdutor
                );
        }
}