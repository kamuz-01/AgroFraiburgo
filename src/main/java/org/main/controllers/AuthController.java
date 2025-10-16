package org.main.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.main.DTOs.LoginRequest;
import org.main.DTOs.LoginResponse;
import org.main.enums.StatusConta;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UsuarioService usuarioService,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // Primeiro checa se o usuário existe e se está pendente
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorNomeLogin(request.getNomeLogin());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Usuário ou senha inválidos!"
            ));
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getStatusConta() == StatusConta.PENDENTE) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Sua conta está pendente de aprovação por um moderador."
            ));
        }

        // Autentica no Spring Security
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getNomeLogin(), request.getSenha())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

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
                .secure(false) // alterar para true em produção com HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtTtl)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new LoginResponse(jwt, "Login realizado com sucesso!", tipoUsuario));
    }

    // --------------------------
    // Logout
    // --------------------------
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("AF_AUTH", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    // --------------------------
    // Login com Facebook (mantido)
    // --------------------------
    @PostMapping("/facebook")
    public ResponseEntity<?> loginComFacebook(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        if (accessToken == null) return ResponseEntity.badRequest().build();

        String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> profile = response.getBody();

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

        return ResponseEntity.status(401).build();
    }
}