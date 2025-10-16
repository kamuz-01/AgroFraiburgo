package org.main.models;

import java.util.Collection;
import java.util.List;
import org.main.models.UsuarioLogado;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UsuarioLogado extends User {
	private static final long serialVersionUID = 1L;
	private final Usuario USUARIO;

    public UsuarioLogado(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        super(usuario.getNomeLogin(), usuario.getSenha(), authorities);
        this.USUARIO = usuario;
    }
    
    // 🔹 Construtor auxiliar que cria as authorities a partir do tipo de usuário
    public UsuarioLogado(Usuario usuario) {
        super(usuario.getNomeLogin(),
              usuario.getSenha(),
              List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name()))
        );
        this.USUARIO = usuario;
    }

    public Usuario getUsuario() {
        return USUARIO;
    }
    
    public Integer getId() {
        return USUARIO.getIdUsuario();
    }
}