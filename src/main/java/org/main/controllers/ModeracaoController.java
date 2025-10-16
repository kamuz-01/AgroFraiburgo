package org.main.controllers;

import java.util.Map;

import org.main.enums.StatusConta;
import org.main.services.ModeradoresService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/moderacao")
public class ModeracaoController {

    private final ModeradoresService moderadoresService;

    public ModeracaoController(ModeradoresService moderadoresService) {
        this.moderadoresService = moderadoresService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(moderadoresService.listarTodosUsuarios());
    }

    @PostMapping("/usuarios/{id}/status")
    public ResponseEntity<?> alterarStatusUsuario(
            @PathVariable Integer id,
            @RequestParam StatusConta novoStatus,
            @RequestParam(required = false) String observacao) {
        try {
            moderadoresService.alterarStatus(id, novoStatus, observacao);

            // Envia pro RabbitMQ se for bloqueio
            if (novoStatus == StatusConta.BLOQUEADO && observacao != null && !observacao.isBlank()) {
                String mensagem = "Usuário " + id + " foi BLOQUEADO. Motivo: " + observacao;
                moderadoresService.enviarMensagemRabbit(mensagem);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Status atualizado com sucesso!",
                    "status", novoStatus
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao alterar status: " + e.getMessage());
        }
    }
}