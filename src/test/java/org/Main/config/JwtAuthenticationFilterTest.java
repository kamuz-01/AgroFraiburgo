package org.Main.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.config.JwtAuthenticationFilter;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UsuarioRepository usuarioRepository;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        usuarioRepository = mock(UsuarioRepository.class);
        filter = new JwtAuthenticationFilter(jwtService, usuarioRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarTokenValidoQuandoUsuarioEstiverAtivo() throws Exception {
        String token = "token-valido";
        Usuario usuario = usuario(StatusConta.ATIVO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AF_AUTH", token)});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(123L);
        when(usuarioRepository.findById(123)).thenReturn(Optional.of(usuario));

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("123");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CONSUMIDOR");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarTokenValidoQuandoUsuarioEstiverBloqueado() throws Exception {
        String token = "token-valido";
        Usuario usuario = usuario(StatusConta.BLOQUEADO);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("AF_AUTH", token)});
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUserId(token)).thenReturn(123L);
        when(usuarioRepository.findById(123)).thenReturn(Optional.of(usuario));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private Usuario usuario(StatusConta statusConta) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(123);
        usuario.setTipoUsuario(TipoUsuario.CONSUMIDOR);
        usuario.setStatusConta(statusConta);
        return usuario;
    }
}
