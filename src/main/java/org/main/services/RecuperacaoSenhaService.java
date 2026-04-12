package org.main.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.main.models.RecuperacaoSenhaToken;
import org.main.models.Usuario;
import org.main.repository.RecuperacaoSenhaTokenRepository;
import org.main.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RecuperacaoSenhaService {

    private static final int MINUTOS_EXPIRACAO_TOKEN = 60;

    private final UsuarioRepository usuarioRepository;
    private final RecuperacaoSenhaTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository,
                                   RecuperacaoSenhaTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email, String baseUrl) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        String emailNormalizado = email.trim();
        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);
        if (usuario == null) {
            return;
        }

        tokenRepository.deleteAllByUsuarioId(usuario.getIdUsuario());

        RecuperacaoSenhaToken token = new RecuperacaoSenhaToken();
        token.setUsuario(usuario);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiraEm(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACAO_TOKEN));
        tokenRepository.save(token);

        String link = montarLink(baseUrl, token.getToken());
        String corpo = "Olá, " + usuario.getNome() + ",\n\n"
                + "Recebemos uma solicitação para redefinir sua senha na AgroFraiburgo.\n\n"
                + "Acesse o link abaixo para criar uma nova senha:\n"
                + link + "\n\n"
                + "Este link expira em 60 minutos. Se você não solicitou essa ação, ignore esta mensagem.";

        emailService.enviarEmail(usuario.getEmail(), "Recuperação de senha - AgroFraiburgo", corpo);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha, String confirmarSenha) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token inválido.");
        }
        if (!StringUtils.hasText(novaSenha)) {
            throw new IllegalArgumentException("Informe a nova senha.");
        }
        if (!novaSenha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas informadas não conferem.");
        }

        RecuperacaoSenhaToken tokenValido = tokenRepository
                .findByTokenAndUsadoEmIsNullAndExpiraEmAfter(token.trim(), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado."));

        Usuario usuario = tokenValido.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        tokenValido.setUsadoEm(LocalDateTime.now());
        tokenRepository.save(tokenValido);
    }

    public boolean tokenValido(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        return tokenRepository
                .findByTokenAndUsadoEmIsNullAndExpiraEmAfter(token.trim(), LocalDateTime.now())
                .isPresent();
    }

    private String montarLink(String baseUrl, String token) {
        String baseNormalizado = baseUrl == null ? "" : baseUrl.trim();
        if (baseNormalizado.endsWith("/")) {
            baseNormalizado = baseNormalizado.substring(0, baseNormalizado.length() - 1);
        }
        return baseNormalizado + "/redefinir_senha?token=" + token;
    }
}
