package org.main.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRefreshFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final long jwtTtl;
    private final UsuarioRepository usuarioRepository;
    private final boolean cookieSecure;
    private final long refreshWindowSeconds;

    public JwtRefreshFilter(JwtService jwtService, UsuarioRepository usuarioRepository,
                            @Value("${jwt.access-token-ttl-seconds}") long jwtTtl,
                            @Value("${app.cookie.secure:false}") boolean cookieSecure,
                            @Value("${jwt.refresh-window-seconds:600}") long refreshWindowSeconds) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.jwtTtl = jwtTtl;
        this.cookieSecure = cookieSecure;
        this.refreshWindowSeconds = refreshWindowSeconds;
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extrairTokenCookie(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && token != null && jwtService.validateToken(token)) {
            long remainingSeconds = jwtService.getTokenRemainingSeconds(token);
            if (remainingSeconds > 0 && remainingSeconds <= refreshWindowSeconds) {
                Long usuarioId = jwtService.extractUserId(token);
                if (usuarioId == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Busca usuário local pelo ID do próprio JWT
                Usuario usuarioLocal = usuarioRepository.findById(usuarioId.intValue())
                        .orElse(null);

                if (usuarioLocal != null) {
                    List<String> roles = auth.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList();

                    // Gera claims a partir do usuário local para preservar os dados do token original
                    Map<String, Object> claims = JwtService.defaultClaimsFromUsuario(usuarioLocal, roles);
                    String novoToken = jwtService.generateToken(claims);

                    Cookie cookie = new Cookie("AF_AUTH", novoToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(cookieSecure);
                    cookie.setPath("/");
                    cookie.setMaxAge((int) jwtTtl);
                    cookie.setAttribute("SameSite", cookieSecure ? "None" : "Lax");
                    response.addCookie(cookie);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extrairTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("AF_AUTH".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}