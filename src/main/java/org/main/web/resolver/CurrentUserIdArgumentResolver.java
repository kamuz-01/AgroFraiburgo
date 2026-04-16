package org.main.web.resolver;

import org.main.models.Usuario;
import org.main.services.JwtService;
import org.main.services.UsuarioService;
import org.main.web.annotation.CurrentUserId;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public CurrentUserIdArgumentResolver(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && (Integer.class.equals(parameter.getParameterType()) || int.class.equals(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        if (auth instanceof UsernamePasswordAuthenticationToken) {
            String principal = auth.getName();
            if (principal == null || principal.isBlank()) {
                return null;
            }

            if (principal.matches("\\d+")) {
                return Integer.valueOf(principal);
            }

            return usuarioService.buscarPorNomeLogin(principal)
                    .map(Usuario::getIdUsuario)
                    .orElse(null);
        }

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            Long uidLong = jwtService.extractUserId(token);
            return uidLong != null ? uidLong.intValue() : null;
        }

        return null;
    }
}