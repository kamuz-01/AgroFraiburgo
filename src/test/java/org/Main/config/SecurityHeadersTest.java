package org.Main.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.main.config.CustomAuthFailureHandler;
import org.main.config.JwtAuthenticationFilter;
import org.main.config.JwtRefreshFilter;
import org.main.services.JwtService;
import org.main.services.LoginProtecaoService;
import org.main.services.LoginRateLimitService;
import org.main.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtRefreshFilter jwtRefreshFilter;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private LoginProtecaoService loginProtecaoService;

    @MockitoBean
    private LoginRateLimitService loginRateLimitService;

    @MockitoBean
    private CustomAuthFailureHandler customAuthFailureHandler;

    @Test
    void deveAdicionarHeadersDeSegurancaNaResposta() throws Exception {
        mockMvc.perform(get("/api/auth/me").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("style-src 'self' 'unsafe-inline' https:")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self' 'unsafe-inline' https:")))
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Strict-Transport-Security", containsString("max-age=31536000")));
    }
}