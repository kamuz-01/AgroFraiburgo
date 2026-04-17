package org.main.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.main.services.LoginProtecaoService;
import org.main.services.LoginRateLimitService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

        private final LoginProtecaoService loginProtecaoService;
        private final LoginRateLimitService loginRateLimitService;

        public CustomAuthFailureHandler(LoginProtecaoService loginProtecaoService,
                                        LoginRateLimitService loginRateLimitService) {
                this.loginProtecaoService = loginProtecaoService;
                this.loginRateLimitService = loginRateLimitService;
        }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
                String nomeLogin = request.getParameter("username");
                if (nomeLogin == null || nomeLogin.isBlank()) {
                        nomeLogin = request.getParameter("nomeLogin");
                }

                if (nomeLogin != null && !nomeLogin.isBlank()) {
                        String ip = extrairIpCliente(request);

                        var bloqueioIpAtual = loginRateLimitService.verificarBloqueio(ip);
                        if (bloqueioIpAtual.isPresent()) {
                                var bloqueio = bloqueioIpAtual.get();
                                response.setStatus(bloqueio.status().value());
                                response.setContentType("application/json;charset=UTF-8");
                                response.setHeader("Retry-After", String.valueOf(bloqueio.retryAfterSeconds()));
                                new ObjectMapper().writeValue(response.getWriter(), Map.of("error", bloqueio.message()));
                                return;
                        }

                        if (exception instanceof LockedException) {
                                loginRateLimitService.registrarFalha(ip, nomeLogin);
                                String mensagem = loginProtecaoService.mensagemBloqueioAtual(nomeLogin)
                                                .orElse("Sua conta está bloqueada.");
                                response.setStatus(HttpStatus.LOCKED.value());
                                response.setContentType("application/json;charset=UTF-8");
                                new ObjectMapper().writeValue(response.getWriter(), Map.of("error", mensagem));
                                return;
                        }

                        var resultadoUsuario = loginProtecaoService.registrarFalha(nomeLogin);
                        var resultadoIp = loginRateLimitService.registrarFalha(ip, nomeLogin);

                        if (resultadoUsuario.isPresent() && resultadoUsuario.get().bloqueado()) {
                                response.setStatus(HttpStatus.LOCKED.value());
                                response.setContentType("application/json;charset=UTF-8");
                                new ObjectMapper().writeValue(response.getWriter(), Map.of("error", resultadoUsuario.get().mensagem()));
                                return;
                        }

                        if (resultadoIp.isPresent()) {
                                var bloqueio = resultadoIp.get();
                                response.setStatus(bloqueio.status().value());
                                response.setContentType("application/json;charset=UTF-8");
                                response.setHeader("Retry-After", String.valueOf(bloqueio.retryAfterSeconds()));
                                new ObjectMapper().writeValue(response.getWriter(), Map.of("error", bloqueio.message()));
                                return;
                        }

                        if (resultadoUsuario.isPresent() && resultadoUsuario.get().aviso()) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json;charset=UTF-8");
                                new ObjectMapper().writeValue(response.getWriter(), Map.of("error", resultadoUsuario.get().mensagem()));
                                return;
                        }
                }

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        new ObjectMapper().writeValue(response.getWriter(), Map.of(
                                "error", "Usuário ou senha inválidos"
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
}