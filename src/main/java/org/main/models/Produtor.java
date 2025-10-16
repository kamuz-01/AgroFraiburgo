package org.main.models;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "produtores")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "feiras"})
public class Produtor {
    @Id
    private Integer idProdutor;

    private Integer avaliacoesRecebidas;

    @JsonIgnore
    @OneToMany(mappedBy = "produtor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutorFeira> feiras = new ArrayList<>();
}