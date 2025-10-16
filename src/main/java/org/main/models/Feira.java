package org.main.models;

import org.main.enums.StatusFeira;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feira")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Feira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feira")
    private Integer idFeira;

    @ManyToOne
    @JoinColumn(name = "id_moderador", nullable = false)
    private Usuario moderador;

    @Column(name = "nome_local", nullable = false, length = 255)
    private String nomeLocal;

    @Column(nullable = false, length = 255)
    private String logradouro;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 255)
    private String bairro;

    @Column(length = 255)
    private String complemento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_feira", nullable = false)
    private StatusFeira statusFeira;
}