package org.main.controllers;

import org.main.models.UsuarioLogado;
import org.main.services.FavoritoProdutoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoProdutoController {

    private final FavoritoProdutoService favoritoProdutoService;
    
    public FavoritoProdutoController(FavoritoProdutoService favoritoProdutoService) {
        this.favoritoProdutoService = favoritoProdutoService;
    }

    @PostMapping("/produtos/{idProduto}/toggle")
    public FavoritoProdutoService.ToggleResultado toggle(Authentication authentication,
                                                         @PathVariable Integer idProduto) {
        Integer idUsuario = currentUserId(authentication);
        try {
            return favoritoProdutoService.toggle(idUsuario, idProduto);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private Integer currentUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UsuarioLogado u) {
            return u.getId();
        }

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object uid = jwtAuth.getTokenAttributes().get("uid");
            if (uid instanceof Number n) {
                return n.intValue();
            }
            if (uid instanceof String s) {
                try {
                    return Integer.valueOf(s);
                } catch (Exception ignored) {}
            }
        }

        // fallback: em alguns fluxos antigos o name pode vir como id
        try {
            return Integer.valueOf(auth.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não foi possível identificar o usuário");
        }
    }
}
