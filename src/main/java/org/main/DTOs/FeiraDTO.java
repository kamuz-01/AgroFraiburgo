package org.main.DTOs;

import org.main.enums.StatusFeira;

import lombok.Data;

@Data
public class FeiraDTO {
    private String nome_local;
    private String logradouro;
    private Integer numero;
    private String bairro;
    private String complemento;
    private StatusFeira status_feira;
}