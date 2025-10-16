package org.main.models;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "produtores_feira")
@Data
public class ProdutorFeira {

    @EmbeddedId
    private ProdutorFeiraId id = new ProdutorFeiraId();

    @ManyToOne
    @MapsId("idProdutor")
    @JoinColumn(name = "id_produtor")
    private Produtor produtor;

    @ManyToOne
    @MapsId("idFeira")
    @JoinColumn(name = "id_feira")
    private Feira feira;
}