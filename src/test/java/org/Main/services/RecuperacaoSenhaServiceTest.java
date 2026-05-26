package org.Main.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.models.RecuperacaoSenhaToken;
import org.main.models.Usuario;
import org.main.repository.RecuperacaoSenhaTokenRepository;
import org.main.repository.UsuarioRepository;
import org.main.services.EmailService;
import org.main.services.RecuperacaoSenhaService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecuperacaoSenhaServiceTest {

    private UsuarioRepository usuarioRepository;
    private RecuperacaoSenhaTokenRepository tokenRepository;
    private EmailService emailService;
    private RecuperacaoSenhaService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        tokenRepository = mock(RecuperacaoSenhaTokenRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        service = new RecuperacaoSenhaService(
                usuarioRepository,
                tokenRepository,
                passwordEncoder,
                emailService,
                2,
                5,
                60,
                10,
                15);
    }

    @Test
    void solicitarRecuperacaoSenhaDeveSalvarHashDoTokenENaoTokenBruto() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNome("Ana");
        usuario.setEmail("ana@email.com");

        when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.save(any(RecuperacaoSenhaToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.solicitarRecuperacaoSenha("ana@email.com", "https://agro.test", "10.0.0.1");

        var tokenCaptor = org.mockito.ArgumentCaptor.forClass(RecuperacaoSenhaToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        var corpoCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmailHtml(eq("ana@email.com"), eq("Recuperação de senha - AgroFraiburgo"), corpoCaptor.capture());

        String tokenBruto = extrairTokenDoEmail(corpoCaptor.getValue());
        String tokenPersistido = tokenCaptor.getValue().getToken();

        assertThat(tokenBruto).matches("[0-9a-fA-F-]{36}");
        assertThat(tokenPersistido).isEqualTo(sha256Hex(tokenBruto));
        assertThat(tokenPersistido).hasSize(64);
        assertThat(tokenPersistido).isNotEqualTo(tokenBruto);
    }

    @Test
    void tokenValidoDeveConsultarRepositorioComHashSha256DoTokenRecebido() {
        String tokenBruto = "550e8400-e29b-41d4-a716-446655440000";

        service.tokenValido(tokenBruto);

        verify(tokenRepository).findByTokenAndUsadoEmIsNullAndExpiraEmAfter(
                eq(sha256Hex(tokenBruto)),
                any(LocalDateTime.class));
    }

    private String extrairTokenDoEmail(String corpoHtml) {
        var matcher = Pattern.compile("token=([0-9a-fA-F-]{36})").matcher(corpoHtml);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
