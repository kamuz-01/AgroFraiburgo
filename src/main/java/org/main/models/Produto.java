package org.main.models;

import java.time.LocalDateTime;
import org.main.enums.StatusProduto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produtor", nullable = false)
    @JsonIgnore
    private Produtor produtor;

    @Column(name = "nome_produto", nullable = false, length = 255)
    private String nomeProduto;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "preco", nullable = false)
    private Double preco;

    @Column(name = "unidade_medida", length = 50)
    private String unidadeMedida = "kg";

    @Column(name = "quantidade_estoque", nullable = false)
    private Double quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_produto", nullable = false)
    private StatusProduto statusProduto = StatusProduto.COM_ESTOQUE;

    @Column(name = "imagem_produto", nullable = false, length = 255)
    private String imagemProduto;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}