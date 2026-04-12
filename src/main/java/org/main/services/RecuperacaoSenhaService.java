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
        String corpoHtml = construirEmailHtml(usuario, link);

        emailService.enviarEmailHtml(usuario.getEmail(), "Recuperação de senha - AgroFraiburgo", corpoHtml);
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

    private String construirEmailHtml(Usuario usuario, String link) {
        String nomeSeguro = escapeHtml(usuario.getNome() != null ? usuario.getNome() : "");
        String linkSeguro = escapeHtml(link);

        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Recuperação de senha - AgroFraiburgo</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f0e6;font-family:Arial,Helvetica,sans-serif;color:#17311f;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:linear-gradient(180deg,#f8fbf6 0%%,#eef4ec 100%%);padding:32px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:640px;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 18px 50px rgba(18,40,26,0.12);border:1px solid rgba(23,49,31,0.08);">
                                    <tr>
                                        <td style="padding:28px 32px;background:linear-gradient(135deg,#1f7a42,#2f8d52);color:#ffffff;">
                                            <div style="font-size:12px;letter-spacing:0.16em;text-transform:uppercase;font-weight:700;opacity:0.9;">AgroFraiburgo</div>
                                            <h1 style="margin:10px 0 0;font-size:30px;line-height:1.05;font-weight:800;">Recuperação de senha</h1>
                                            <p style="margin:14px 0 0;font-size:15px;line-height:1.6;color:rgba(255,255,255,0.92);">Um novo link foi solicitado para a sua conta. Use o botão abaixo para criar uma senha nova com segurança.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <p style="margin:0 0 16px;font-size:16px;line-height:1.7;">Olá, <strong>%s</strong>.</p>
                                            <p style="margin:0 0 18px;font-size:15px;line-height:1.7;color:#5a6b5d;">Recebemos uma solicitação para redefinir a senha da sua conta na AgroFraiburgo. Se foi você, clique no botão abaixo para continuar.</p>

                                            <div style="text-align:center;margin:30px 0;">
                                                <a href="%s" style="display:inline-block;background:linear-gradient(135deg,#2e7d4a,#3d8e57);color:#ffffff;text-decoration:none;font-weight:800;padding:14px 26px;border-radius:999px;box-shadow:0 14px 28px rgba(46,125,74,0.24);">Redefinir minha senha</a>
                                            </div>

                                            <div style="background:#f7faf4;border:1px solid rgba(23,49,31,0.08);border-radius:18px;padding:18px 20px;">
                                                <p style="margin:0;font-size:14px;line-height:1.7;color:#5a6b5d;">Se o botão não funcionar, copie e cole este link no navegador:</p>
                                                <p style="margin:10px 0 0;font-size:13px;line-height:1.6;word-break:break-all;color:#1f7a42;">%s</p>
                                            </div>

                                            <p style="margin:20px 0 0;font-size:13px;line-height:1.7;color:#5a6b5d;">Este link expira em 60 minutos. Se você não solicitou a recuperação, pode ignorar este e-mail com segurança.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:18px 32px 28px;background:#f9fbf8;border-top:1px solid rgba(23,49,31,0.08);font-size:12px;line-height:1.6;color:#6b7b6d;">
                                            AgroFraiburgo - agricultura familiar, confiança e proximidade.
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(nomeSeguro, linkSeguro, linkSeguro);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
