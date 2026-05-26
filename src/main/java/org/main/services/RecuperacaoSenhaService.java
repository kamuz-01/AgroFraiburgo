package org.main.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.main.models.RecuperacaoSenhaToken;
import org.main.models.Usuario;
import org.main.repository.RecuperacaoSenhaTokenRepository;
import org.main.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final int intervaloMinimoEmailMinutos;
    private final int maxSolicitacoesEmail;
    private final int janelaEmailMinutos;
    private final int maxSolicitacoesIp;
    private final int janelaIpMinutos;
    private final long tempoMinimoRespostaMillis;

    public RecuperacaoSenhaService(UsuarioRepository usuarioRepository,
                                   RecuperacaoSenhaTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService,
                                   @Value("${app.security.password-reset.email-min-interval-minutes:2}") int intervaloMinimoEmailMinutos,
                                   @Value("${app.security.password-reset.email.max-attempts:5}") int maxSolicitacoesEmail,
                                   @Value("${app.security.password-reset.email.window-minutes:60}") int janelaEmailMinutos,
                                   @Value("${app.security.password-reset.ip.max-attempts:10}") int maxSolicitacoesIp,
                                   @Value("${app.security.password-reset.ip.window-minutes:15}") int janelaIpMinutos,
                                   @Value("${app.security.password-reset.min-response-millis:500}") long tempoMinimoRespostaMillis) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.intervaloMinimoEmailMinutos = intervaloMinimoEmailMinutos;
        this.maxSolicitacoesEmail = maxSolicitacoesEmail;
        this.janelaEmailMinutos = janelaEmailMinutos;
        this.maxSolicitacoesIp = maxSolicitacoesIp;
        this.janelaIpMinutos = janelaIpMinutos;
        this.tempoMinimoRespostaMillis = Math.max(0, tempoMinimoRespostaMillis);
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email, String baseUrl, String ipAddress) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }

        long inicio = System.nanoTime();
        try {
            String emailNormalizado = email.trim();
            String ipNormalizado = normalizarIp(ipAddress);
            LocalDateTime agora = LocalDateTime.now();

            if (ipAtingiuLimite(ipNormalizado, agora)) {
                return;
            }

            Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);
            if (usuario == null) {
                executarTrabalhoFicticio(baseUrl);
                return;
            }

            if (usuarioAtingiuLimite(usuario.getIdUsuario(), agora)) {
                executarTrabalhoFicticio(baseUrl);
                return;
            }

            tokenRepository.marcarTokensAtivosComoUsados(usuario.getIdUsuario(), agora);

            String tokenBruto = UUID.randomUUID().toString();

            RecuperacaoSenhaToken token = new RecuperacaoSenhaToken();
            token.setUsuario(usuario);
            token.setToken(hashToken(tokenBruto));
            token.setExpiraEm(agora.plusMinutes(MINUTOS_EXPIRACAO_TOKEN));
            token.setIpAddress(ipNormalizado);
            tokenRepository.save(token);

            String link = montarLink(baseUrl, tokenBruto);
            String corpoHtml = construirEmailHtml(usuario, link);

            emailService.enqueueEmail(usuario.getEmail(), "Recuperação de senha - AgroFraiburgo", corpoHtml);
        } finally {
            aguardarTempoMinimo(inicio);
        }
    }

    private void executarTrabalhoFicticio(String baseUrl) {
        String tokenFicticio = UUID.randomUUID().toString();
        hashToken(tokenFicticio);

        Usuario usuarioFicticio = new Usuario();
        usuarioFicticio.setNome("");
        String linkFicticio = montarLink(baseUrl, tokenFicticio);
        construirEmailHtml(usuarioFicticio, linkFicticio);
    }

    private boolean ipAtingiuLimite(String ipAddress, LocalDateTime agora) {
        if (!StringUtils.hasText(ipAddress)) {
            return false;
        }

        long solicitacoes = tokenRepository.countByIpAddressAndCriadoEmAfter(
                ipAddress,
                agora.minusMinutes(janelaIpMinutos));
        return solicitacoes >= maxSolicitacoesIp;
    }

    private boolean usuarioAtingiuLimite(Integer idUsuario, LocalDateTime agora) {
        boolean solicitouRecentemente = tokenRepository.existsByUsuario_IdUsuarioAndCriadoEmAfter(
                idUsuario,
                agora.minusMinutes(intervaloMinimoEmailMinutos));
        if (solicitouRecentemente) {
            return true;
        }

        long solicitacoesNaJanela = tokenRepository.countByUsuario_IdUsuarioAndCriadoEmAfter(
                idUsuario,
                agora.minusMinutes(janelaEmailMinutos));
        return solicitacoesNaJanela >= maxSolicitacoesEmail;
    }

    private String normalizarIp(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return null;
        }

        String normalizado = ipAddress.trim();
        return normalizado.length() <= 45 ? normalizado : normalizado.substring(0, 45);
    }

    private void aguardarTempoMinimo(long inicioNanos) {
        if (tempoMinimoRespostaMillis <= 0) {
            return;
        }

        long decorridoMillis = (System.nanoTime() - inicioNanos) / 1_000_000;
        long restanteMillis = tempoMinimoRespostaMillis - decorridoMillis;
        if (restanteMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(restanteMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
                .findByTokenAndUsadoEmIsNullAndExpiraEmAfter(hashToken(token.trim()), LocalDateTime.now())
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
                .findByTokenAndUsadoEmIsNullAndExpiraEmAfter(hashToken(token.trim()), LocalDateTime.now())
                .isPresent();
    }

    static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
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
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" bgcolor="#f4f0e6" style="background-color:#f4f0e6;padding:32px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" bgcolor="#ffffff" style="max-width:640px;background-color:#ffffff;border:1px solid #dfe6db;">
                                    <tr>
                                        <td bgcolor="#1f7a42" style="padding:28px 32px;background-color:#1f7a42;color:#ffffff;">
                                            <div style="font-size:12px;letter-spacing:0.16em;text-transform:uppercase;font-weight:700;color:#dff2e7;">AgroFraiburgo</div>
                                            <h1 style="margin:10px 0 0;font-size:30px;line-height:1.05;font-weight:800;color:#ffffff;">Recuperação de senha</h1>
                                            <p style="margin:14px 0 0;font-size:15px;line-height:1.6;color:#e7f5ec;">Um novo link foi solicitado para a sua conta. Use o botão abaixo para criar uma senha nova com segurança.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <p style="margin:0 0 16px;font-size:16px;line-height:1.7;">Olá, <strong>%s</strong>.</p>
                                            <p style="margin:0 0 18px;font-size:15px;line-height:1.7;color:#5a6b5d;">Recebemos uma solicitação para redefinir a senha da sua conta na AgroFraiburgo. Se foi você, clique no botão abaixo para continuar.</p>

                                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin:28px auto 24px;">
                                                <tr>
                                                    <td align="center" bgcolor="#2e7d4a" style="border-radius:999px;">
                                                        <a href="%s" style="display:inline-block;padding:14px 26px;font-size:15px;font-weight:800;font-family:Arial,Helvetica,sans-serif;color:#ffffff;text-decoration:none;border-radius:999px;">Redefinir minha senha</a>
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="background:#f7faf4;border:1px solid #e2eadf;padding:18px 20px;">
                                                <p style="margin:0;font-size:14px;line-height:1.7;color:#5a6b5d;">Se o botão não funcionar, copie e cole este link no navegador:</p>
                                                <p style="margin:10px 0 0;font-size:13px;line-height:1.6;word-break:break-all;color:#1f7a42;">%s</p>
                                            </div>

                                            <p style="margin:20px 0 0;font-size:13px;line-height:1.7;color:#5a6b5d;">Este link expira em 60 minutos. Se você não solicitou a recuperação, pode ignorar este e-mail com segurança.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td bgcolor="#f9fbf8" style="padding:18px 32px 28px;background-color:#f9fbf8;border-top:1px solid #e2eadf;font-size:12px;line-height:1.6;color:#6b7b6d;">
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
