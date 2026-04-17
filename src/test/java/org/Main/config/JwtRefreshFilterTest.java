package org.Main.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.config.JwtRefreshFilter;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.JwtService;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtRefreshFilterTest {

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private JwtRefreshFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        filter = new JwtRefreshFilter(jwtService, usuarioRepository, 3600, false, 600);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void naoDeveRenovarQuandoTokenAindaNaoEstaPertoDaExpiracao() throws Exception {
        String token = "token-antigo";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "123",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CONSUMIDOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AF_AUTH", token)});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getTokenRemainingSeconds(token)).thenReturn(5000L);

        filter.doFilter(request, response, filterChain);

        verify(jwtService, never()).generateToken(anyMap());
        verify(response, never()).addCookie(org.mockito.ArgumentMatchers.any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveRenovarQuandoTokenEstiverDentroDaJanelaConfigurada() throws Exception {
        String token = "token-quase-expirando";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "123",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CONSUMIDOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(123);
        usuario.setNomeLogin("joao");
        usuario.setEmail("joao@exemplo.com");
        usuario.setImagemPerfil("/img/perfil.png");
        usuario.setImagemCapa("/img/capa.webp");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AF_AUTH", token)});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getTokenRemainingSeconds(token)).thenReturn(300L);
        when(jwtService.extractUserId(token)).thenReturn(123L);
        when(usuarioRepository.findById(123)).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(anyMap())).thenReturn("token-renovado");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getName()).isEqualTo("AF_AUTH");
        assertThat(cookieCaptor.getValue().getValue()).isEqualTo("token-renovado");
        verify(jwtService).generateToken(anyMap());
        verify(filterChain).doFilter(request, response);
    }
}
