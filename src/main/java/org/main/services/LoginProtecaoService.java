package org.main.services;

import org.main.enums.LoginBloqueioEtapa;
import org.main.enums.StatusConta;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginProtecaoService {

    private static final int BLOQUEIO_INICIAL_HORAS = 12;
    private static final int BLOQUEIO_REINCIDENCIA_HORAS = 24;
    private static final int FALHAS_PARA_ESCALONAR = 3;

    private final UsuarioRepository usuarioRepository;

    public LoginProtecaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<String> mensagemBloqueioAtual(String nomeLogin) {
        return usuarioRepository.findByNomeLogin(nomeLogin)
                .flatMap(this::mensagemBloqueioAtual);
    }

    public record LoginFalhaResultado(boolean bloqueado, boolean aviso, String mensagem) {
        public static LoginFalhaResultado nenhum() {
            return new LoginFalhaResultado(false, false, null);
        }

        public static LoginFalhaResultado aviso(String mensagem) {
            return new LoginFalhaResultado(false, true, mensagem);
        }

        public static LoginFalhaResultado bloqueio(String mensagem) {
            return new LoginFalhaResultado(true, false, mensagem);
        }
    }

    public Optional<String> mensagemBloqueioAtual(Usuario usuario) {
        if (usuario == null) {
            return Optional.empty();
        }

        LoginBloqueioEtapa etapa = etapaAtual(usuario);
        if (etapa == LoginBloqueioEtapa.BLOQUEIO_DEFINITIVO) {
            return Optional.of("Sua conta foi bloqueada definitivamente após tentativas inválidas de autenticação.");
        }

        LocalDateTime bloqueadoAte = usuario.getLoginBloqueadoAte();
        if (bloqueadoAte == null) {
            return Optional.empty();
        }

        LocalDateTime agora = LocalDateTime.now();
        if (!agora.isBefore(bloqueadoAte)) {
            return Optional.empty();
        }

        return switch (etapa) {
            case BLOQUEIO_12H -> Optional.of("Sua conta está bloqueada por 12 horas após tentativas inválidas de autenticação.");
            case BLOQUEIO_24H -> Optional.of("Sua conta está bloqueada por 24 horas após tentativas inválidas de autenticação.");
            default -> Optional.empty();
        };
    }

    @Transactional
    public Optional<LoginFalhaResultado> registrarFalha(String nomeLogin) {
        Usuario usuario = usuarioRepository.findByNomeLogin(nomeLogin).orElse(null);
        if (usuario == null || usuario.getStatusConta() != StatusConta.ATIVO) {
            return Optional.empty();
        }

        LoginBloqueioEtapa etapa = etapaAtual(usuario);
        LocalDateTime agora = LocalDateTime.now();

        if ((etapa == LoginBloqueioEtapa.BLOQUEIO_12H || etapa == LoginBloqueioEtapa.BLOQUEIO_24H)
                && usuario.getLoginBloqueadoAte() != null
                && agora.isBefore(usuario.getLoginBloqueadoAte())) {
            return mensagemBloqueioAtual(usuario).map(LoginFalhaResultado::bloqueio);
        }

        int falhas = quantidadeFalhas(usuario) + 1;
        if (falhas == 2) {
            usuario.setLoginFalhasConsecutivas(falhas);
            usuarioRepository.save(usuario);
            return Optional.of(LoginFalhaResultado.aviso(
                    "Esta foi sua segunda tentativa falha consecutiva. Se a próxima também falhar, sua conta poderá ser bloqueada. Se não lembra da senha, use a recuperação por e-mail agora para evitar o bloqueio."));
        }

        if (falhas < FALHAS_PARA_ESCALONAR) {
            usuario.setLoginFalhasConsecutivas(falhas);
            usuarioRepository.save(usuario);
            return Optional.empty();
        }

        usuario.setLoginFalhasConsecutivas(0);
        switch (etapa) {
            case LIVRE -> {
                usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_12H);
                usuario.setLoginBloqueadoAte(agora.plusHours(BLOQUEIO_INICIAL_HORAS));
                usuarioRepository.save(usuario);
                return mensagemBloqueioAtual(usuario).map(LoginFalhaResultado::bloqueio);
            }
            case BLOQUEIO_12H -> {
                usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_24H);
                usuario.setLoginBloqueadoAte(agora.plusHours(BLOQUEIO_REINCIDENCIA_HORAS));
                usuarioRepository.save(usuario);
                return mensagemBloqueioAtual(usuario).map(LoginFalhaResultado::bloqueio);
            }
            case BLOQUEIO_24H -> {
                usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_DEFINITIVO);
                usuario.setLoginBloqueadoAte(null);
                usuario.setStatusConta(StatusConta.BLOQUEADO);
                usuarioRepository.save(usuario);
                return mensagemBloqueioAtual(usuario).map(LoginFalhaResultado::bloqueio);
            }
            case BLOQUEIO_DEFINITIVO -> {
                usuarioRepository.save(usuario);
                return mensagemBloqueioAtual(usuario).map(LoginFalhaResultado::bloqueio);
            }
        }

        return Optional.empty();
    }

    @Transactional
    public void registrarSucesso(Usuario usuario) {
        if (usuario == null || usuario.getStatusConta() != StatusConta.ATIVO) {
            return;
        }

        usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.LIVRE);
        usuario.setLoginFalhasConsecutivas(0);
        usuario.setLoginBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }

    private int quantidadeFalhas(Usuario usuario) {
        return usuario.getLoginFalhasConsecutivas() == null ? 0 : usuario.getLoginFalhasConsecutivas();
    }

    private LoginBloqueioEtapa etapaAtual(Usuario usuario) {
        return usuario.getLoginBloqueioEtapa() == null ? LoginBloqueioEtapa.LIVRE : usuario.getLoginBloqueioEtapa();
    }
}