package org.main.DTOs;

import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModeracaoUsuarioDTO {

    private Integer idUsuario;
    private String nome;
    private String sobrenome;
    private String cpf;
    private TipoUsuario tipoUsuario;
    private String cidade;
    private String estado;
    private StatusConta statusConta;

    public static ModeracaoUsuarioDTO fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new ModeracaoUsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getCpf(),
                usuario.getTipoUsuario(),
                usuario.getCidade(),
                usuario.getEstado(),
                usuario.getStatusConta()
        );
    }
}