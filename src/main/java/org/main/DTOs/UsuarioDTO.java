package org.main.DTOs;

import java.time.LocalDateTime;

import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UsuarioDTO {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nome_usuario")
    private String nome;

    @Column(name = "sobrenome_usuario")
    private String sobrenome;

    @Column(name = "cpf_usuario")
    private String cpf;

    @Column(name = "sexo")
    private String sexo;
    
    @Column(name = "criado_em", updatable = false, insertable = false) 
    private LocalDateTime criadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario = TipoUsuario.CONSUMIDOR;
    
	@Column(name = "cidade")
	private String cidade;
	
	@Column(name = "estado")
	private String estado;
	
	@Enumerated(EnumType.STRING) @Column(name = "status_conta", nullable = false) 
	private StatusConta statusConta = StatusConta.ATIVO; 
}