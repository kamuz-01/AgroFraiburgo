package org.main.services;

import org.main.enums.StatusConta;
import org.main.models.Usuario;
import org.main.models.UsuarioLogado;
import org.main.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.LockedException;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LoginProtecaoService loginProtecaoService;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository, LoginProtecaoService loginProtecaoService) {
        this.usuarioRepository = usuarioRepository;
        this.loginProtecaoService = loginProtecaoService;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // Tenta login tradicional
        Optional<Usuario> optUsuario = usuarioRepository.findByNomeLogin(login);

        Usuario usuario;
        if (optUsuario.isPresent()) {
            usuario = optUsuario.get();
        } else {
            throw new UsernameNotFoundException("Usuário não encontrado: " + login);
        }

        if (usuario.getStatusConta() == StatusConta.PENDENTE) {
            throw new UsernameNotFoundException("Conta pendente de aprovação");
        }

        if (usuario.getStatusConta() == StatusConta.REJEITADO) {
            throw new LockedException("Conta rejeitada");
        }

        if (usuario.getStatusConta() == StatusConta.BLOQUEADO) {
            throw new LockedException("Conta bloqueada");
        }

        loginProtecaoService.mensagemBloqueioAtual(usuario)
                .ifPresent(mensagem -> {
                    throw new LockedException(mensagem);
                });

        String role = "ROLE_" + usuario.getTipoUsuario().name();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new UsuarioLogado(usuario, authorities);
    }

    // Método auxiliar para OAuth
    public UserDetails loadUserByOauth(String provider, String oauthId) {
        Usuario usuario = usuarioRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário OAuth não encontrado: " + provider + " / " + oauthId));

        if (usuario.getStatusConta() == StatusConta.PENDENTE) {
            throw new UsernameNotFoundException("Conta pendente de aprovação");
        }

        if (usuario.getStatusConta() == StatusConta.REJEITADO) {
            throw new LockedException("Conta rejeitada");
        }

        if (usuario.getStatusConta() == StatusConta.BLOQUEADO) {
            throw new LockedException("Conta bloqueada");
        }

        String role = "ROLE_" + usuario.getTipoUsuario().name();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new UsuarioLogado(usuario, authorities);
    }
}