package org.main.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.main.DTOs.LoginRequest;
import org.main.enums.StatusConta;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.LoginRateLimitService;
import org.main.services.LoginProtecaoService;
import org.main.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginProtecaoService loginProtecaoService;
    private final LoginRateLimitService loginRateLimitService;

    public AuthController(UsuarioService usuarioService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          LoginProtecaoService loginProtecaoService,
                          LoginRateLimitService loginRateLimitService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.loginProtecaoService = loginProtecaoService;
        this.loginRateLimitService = loginRateLimitService;
    }

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    // --------------------------
    // Me
    // --------------------------
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of(
                "sub", jwt.getSubject(),
                "name", jwt.getClaimAsString("name"),
                "provider", jwt.getClaimAsString("provider")
        ));
    }

    // --------------------------
    // Login tradicional
    // --------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        String ip = extrairIpCliente(httpRequest);

        var bloqueioIpAtual = loginRateLimitService.verificarBloqueio(ip);
        if (bloqueioIpAtual.isPresent()) {
            var bloqueio = bloqueioIpAtual.get();
            return ResponseEntity.status(bloqueio.status())
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(bloqueio.retryAfterSeconds()))
                    .body(Map.of("error", bloqueio.message()));
        }

        // Primeiro checa se o usuário existe e se está pendente
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorNomeLogin(request.getNomeLogin());
        if (usuarioOpt.isEmpty()) {
            loginRateLimitService.registrarFalha(ip, request.getNomeLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Usuário ou senha inválidos!"
            ));
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getStatusConta() == StatusConta.PENDENTE) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Sua conta está pendente de aprovação por um moderador."
            ));
        }

        if (usuario.getStatusConta() == StatusConta.REJEITADO) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Sua conta foi rejeitada e não pode acessar a aplicação."
            ));
        }

        loginProtecaoService.mensagemBloqueioAtual(usuario).ifPresent(mensagem -> {
            loginRateLimitService.registrarFalha(ip, request.getNomeLogin());
            throw new org.springframework.security.authentication.LockedException(mensagem);
        });

        // Autentica no Spring Security
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getNomeLogin(), request.getSenha())
            );
        } catch (org.springframework.security.authentication.LockedException e) {
            loginRateLimitService.registrarFalha(ip, request.getNomeLogin());
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (AuthenticationException e) {
            var falhaUsuario = loginProtecaoService.registrarFalha(request.getNomeLogin());
            var falhaIp = loginRateLimitService.registrarFalha(ip, request.getNomeLogin());

            if (falhaUsuario.isPresent() && falhaUsuario.get().bloqueado()) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of(
                "error", falhaUsuario.get().mensagem()
            ));
            }

            if (falhaIp.isPresent()) {
            var bloqueio = falhaIp.get();
            return ResponseEntity.status(bloqueio.status())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(bloqueio.retryAfterSeconds()))
                .body(Map.of("error", bloqueio.message()));
            }

            if (falhaUsuario.isPresent() && falhaUsuario.get().aviso()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", falhaUsuario.get().mensagem()
            ));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Usuário ou senha inválidos!"
            ));
        }
        SecurityContextHolder.getContext().setAuthentication(auth);

        loginRateLimitService.registrarSucesso(ip);
        loginProtecaoService.registrarSucesso(usuario);

        // Gera JWT com UID do usuário
        String jwt = jwtService.generateToken(JwtService.defaultClaims(auth, usuario));

        // pega tipoUsuario
        String tipoUsuario = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("CONSUMIDOR");

        // Seta cookie
        ResponseCookie cookie = ResponseCookie.from("AF_AUTH", jwt)
                .httpOnly(true)
            .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtTtl)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "message", "Login realizado com sucesso!",
                "tipoUsuario", tipoUsuario
        ));
    }

    private String extrairIpCliente(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    // --------------------------
    // Logout
    // --------------------------
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AF_AUTH", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", cookieSecure ? "None" : "Lax");
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    // --------------------------
    // Login com Facebook
    // --------------------------
    @PostMapping("/facebook")
    public ResponseEntity<?> loginComFacebook(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "accessToken ausente"));
        }

        // Monta a URL corretamente e faz encode das chaves { } para não virar template do Spring
        URI uri = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/me")
                .queryParam("fields", "id,name,email,picture.width(400).height(400){url,width,height}")
                .queryParam("access_token", accessToken)
                .build()
                .encode() // <-- essencial: transforma { } em %7B %7D
                .toUri();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> fbResponse;

        try {
            fbResponse = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            // Falha ao chamar Facebook (token inválido, rede, etc.)
            return ResponseEntity.status(401).body(Map.of("error", "Falha ao validar token do Facebook"));
        }

        Map<String, Object> profile = fbResponse.getBody();

        if (profile != null && profile.containsKey("id")) {
            Usuario u = usuarioService.processOAuthPostLogin("facebook", profile);

            Map<String, Object> claims = JwtService.defaultClaims(profile, u);
            String token = jwtService.generateToken(claims);

            ResponseCookie cookie = ResponseCookie.from("AF_AUTH", token)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(jwtTtl)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .build();
        }

        return ResponseEntity.status(401).body(Map.of("error", "Resposta inválida do Facebook"));
    }
}
