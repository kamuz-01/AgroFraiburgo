package org.Main.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.controllers.AuthController;
import org.main.DTOs.LoginRequest;
import org.main.enums.StatusConta;
import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.LoginProtecaoService;
import org.main.services.LoginRateLimitService;
import org.main.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private UsuarioService usuarioService;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private LoginProtecaoService loginProtecaoService;
    private LoginRateLimitService loginRateLimitService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        loginProtecaoService = mock(LoginProtecaoService.class);
        loginRateLimitService = mock(LoginRateLimitService.class);
        controller = new AuthController(usuarioService, jwtService, authenticationManager, loginProtecaoService, loginRateLimitService);
    }

    @Test
    void loginBemSucedidoDeveLimparRateLimitDoIp() {
        LoginRequest request = new LoginRequest();
        request.setNomeLogin("joao");
        request.setSenha("senhaCerta");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(12);
        usuario.setNomeLogin("joao");
        usuario.setEmail("joao@email.com");
        usuario.setStatusConta(StatusConta.ATIVO);
        usuario.setImagemPerfil("perfil.png");
        usuario.setImagemCapa("capa.png");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new User("joao", "senhaCerta", List.of(new SimpleGrantedAuthority("ROLE_CONSUMIDOR"))),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CONSUMIDOR"))
        );

        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("10.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(usuarioService.buscarPorNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(loginProtecaoService.mensagemBloqueioAtual(usuario)).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(anyMap())).thenReturn("token-123");

        var result = controller.login(request, httpRequest, response);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(loginRateLimitService).registrarSucesso("10.0.0.1");
        verify(loginProtecaoService).registrarSucesso(usuario);
        assertThat(response.getHeader("Set-Cookie")).contains("AF_AUTH=token-123");
    }
}
