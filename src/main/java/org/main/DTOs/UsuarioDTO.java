package org.main.DTOs;

import java.time.LocalDateTime;

import org.main.models.Usuario;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioDTO {

    private Integer idUsuario;

    private String nome;

    private String sobrenome;

    private String cpf;

    private String sexo;

    private LocalDateTime criadoEm;

    private TipoUsuario tipoUsuario = TipoUsuario.CONSUMIDOR;

	private String cidade;

	private String estado;

    private StatusConta statusConta = StatusConta.ATIVO;

    public static UsuarioDTO fromEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getCpf(),
                usuario.getSexo(),
                usuario.getCriadoEm(),
                usuario.getTipoUsuario(),
                usuario.getCidade(),
                usuario.getEstado(),
                usuario.getStatusConta()
        );
    }
}