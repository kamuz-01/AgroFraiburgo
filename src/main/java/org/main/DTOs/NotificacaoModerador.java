package org.main.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoModerador {
    private Integer idProdutor;
    private String email;
    private String nome;
    private String novoStatus;
    private String observacao;
}