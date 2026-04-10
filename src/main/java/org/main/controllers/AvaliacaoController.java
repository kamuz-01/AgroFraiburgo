package org.main.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.main.models.Avaliacao;
import org.main.repository.AvaliacaoRepository;
import org.main.services.AvaliacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/avaliacoes")
@Validated
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final AvaliacaoRepository avaliacaoRepository;

    public AvaliacaoController(AvaliacaoService avaliacaoService, AvaliacaoRepository avaliacaoRepository) {
        this.avaliacaoService = avaliacaoService;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @GetMapping("/produtores/{idProdutor}")
    public List<AvaliacaoResponse> listarRecentes(@PathVariable Integer idProdutor) {
        return avaliacaoRepository.findTop10ByIdProdutorOrderByDataAvaliacaoDesc(idProdutor)
                .stream()
                .map(AvaliacaoResponse::from)
                .toList();
    }

    @PostMapping("/produtores/{idProdutor}")
    public AvaliacaoResponse avaliarProdutor(Authentication auth,
                                             @PathVariable Integer idProdutor,
                                             @Valid @RequestBody AvaliacaoRequest body) {
        Integer idConsumidor;
        try {
            idConsumidor = Integer.valueOf(auth.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        }

        try {
            Avaliacao avaliacao = avaliacaoService.criarOuAtualizar(idConsumidor, idProdutor, body.nota(), body.comentario());
            return AvaliacaoResponse.from(avaliacao);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public record AvaliacaoRequest(
            @NotNull @Min(1) @Max(5) Integer nota,
            String comentario
    ) {}

    public record AvaliacaoResponse(
            Integer id,
            Integer idConsumidor,
            Integer idProdutor,
            Integer nota,
            String comentario,
            LocalDateTime dataAvaliacao
    ) {
        static AvaliacaoResponse from(Avaliacao a) {
            return new AvaliacaoResponse(
                    a.getIdAvaliacao(),
                    a.getIdConsumidor(),
                    a.getIdProdutor(),
                    a.getNota(),
                    a.getComentario(),
                    a.getDataAvaliacao()
            );
        }
    }
}
