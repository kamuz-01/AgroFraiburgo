package org.main.config;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRefreshFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final long jwtTtl;
    private final UsuarioService usuarioService;

    public JwtRefreshFilter(JwtService jwtService, UsuarioService usuarioService,
                            @Value("${jwt.access-token-ttl-seconds}") long jwtTtl) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.jwtTtl = jwtTtl;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            // Busca usuário local pelo nome de login
            Usuario usuarioLocal = usuarioService.buscarPorNomeLogin(auth.getName())
                    .orElse(null);

            if (usuarioLocal != null) {
                // Gera claims incluindo fotoPerfil e fotoCapa
                Map<String, Object> claims = JwtService.defaultClaims(auth, usuarioLocal);
                String token = jwtService.generateToken(claims);

                Cookie cookie = new Cookie("AF_AUTH", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(true); // usar true em produção com HTTPS
                cookie.setPath("/");
                cookie.setMaxAge((int) jwtTtl);
                cookie.setAttribute("SameSite", "None");
                response.addCookie(cookie);
            }
        }

        filterChain.doFilter(request, response);
    }
}