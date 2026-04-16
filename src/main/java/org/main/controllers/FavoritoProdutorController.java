package org.main.controllers;

import org.main.services.FavoritoProdutorService;
import org.main.web.annotation.CurrentUserId;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoProdutorController {

    private final FavoritoProdutorService favoritoProdutorService;

    public FavoritoProdutorController(FavoritoProdutorService favoritoProdutorService) {
        this.favoritoProdutorService = favoritoProdutorService;
    }

    @PostMapping("/produtores/{idProdutor}/toggle")
    public FavoritoProdutorService.ToggleResultado toggle(@PathVariable Integer idProdutor,
                                                          @CurrentUserId Integer idUsuario) {
        try {
            return favoritoProdutorService.toggle(idUsuario, idProdutor);
        } catch (DataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Funcionalidade indisponível: schema de favoritos ainda não foi aplicado (tabela favoritos_produtores). Reinicie a aplicação para rodar as migrations.");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
