package org.main.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_avaliacao")
    private Integer idAvaliacao;

    @NotNull
    @Column(name = "id_consumidor", nullable = false)
    private Integer idConsumidor;

    @NotNull
    @Column(name = "id_produtor", nullable = false)
    private Integer idProdutor;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(name = "nota", nullable = false)
    private Integer nota;

    @Column(name = "comentario", length = 255)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    @PrePersist
    protected void prePersist() {
        if (dataAvaliacao == null) {
            dataAvaliacao = LocalDateTime.now();
        }
        if (comentario != null) {
            String trimmed = comentario.trim();
            comentario = trimmed.isEmpty() ? null : trimmed;
        }
    }
}
