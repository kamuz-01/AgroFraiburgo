package org.main.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoModeradorDTO {
    private String email;
    private String nome;
    private String novoStatus;
    private String observacao;
}