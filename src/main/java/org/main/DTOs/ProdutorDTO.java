package org.main.DTOs;

import org.main.enums.StatusConta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ProdutorDTO {	
    private Integer id;
    private String nome;
    private String cpf;
    private Integer idDocumento;
    private String documentoIdentidade;
    private String comprovanteResidencia;
    private String declaracaoPronaf;
    private String certificadoProducaoOrganica;
    private String codigoRastreabilidade;
    private String numeroInscricaoEstadual;
    private String alvaraSanitario;
    private StatusConta statusConta;
}