package org.main.DTOs;

import org.main.enums.StatusConta;
import org.main.models.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProdutorPendenteDTO {

    private Integer idUsuario;
    private String nomeCompleto;
    private String cpf;
    private String cidade;
    private String estado;
    private StatusConta statusConta;

    public static ProdutorPendenteDTO fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        String nome = usuario.getNome() != null ? usuario.getNome().trim() : "";
        String sobrenome = usuario.getSobrenome() != null ? usuario.getSobrenome().trim() : "";

        return new ProdutorPendenteDTO(
                usuario.getIdUsuario(),
                (nome + " " + sobrenome).trim(),
                usuario.getCpf(),
                usuario.getCidade(),
                usuario.getEstado(),
                usuario.getStatusConta()
        );
    }
}