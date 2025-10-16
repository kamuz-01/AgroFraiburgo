package org.main.DTOs;

import org.main.enums.StatusConta;
import lombok.Data;

@Data
public class AlterarStatusRequest {
	private Integer idProdutor;
    private StatusConta novoStatus;
    private String observacao;
}