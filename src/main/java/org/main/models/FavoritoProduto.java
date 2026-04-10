package org.main.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "favoritos_produtos")
public class FavoritoProduto {

    @EmbeddedId
    private FavoritoProdutoId id;

    @Column(name = "data_favorito", insertable = false, updatable = false)
    private LocalDateTime dataFavorito;

    public FavoritoProduto(Integer idUsuario, Integer idProduto) {
        this.id = new FavoritoProdutoId(idUsuario, idProduto);
    }
}
