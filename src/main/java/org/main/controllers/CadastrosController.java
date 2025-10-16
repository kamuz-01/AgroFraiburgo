package org.main.controllers;

import lombok.RequiredArgsConstructor;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.models.Usuario;
import org.main.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/cadastro")
@RequiredArgsConstructor
public class CadastrosController {

    private final UsuarioService usuarioService;

    // --------------------------
    // Cadastro de Consumidor
    // --------------------------
    @PostMapping("/consumidor")
    public ResponseEntity<?> cadastrarConsumidor(@ModelAttribute CadastroUsuarioDTO dto) {
        try {
            Usuario salvo = usuarioService.cadastrarConsumidor(dto);
            return ResponseEntity.ok(Map.of(
                "message", "Consumidor cadastrado com sucesso!",
                "idUsuario", salvo.getIdUsuario()
            ));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------------
    // Cadastro de Produtor
    // --------------------------
    @PostMapping("/produtor")
    public ResponseEntity<?> cadastrarProdutor(
            @ModelAttribute CadastroUsuarioDTO dto,
            @ModelAttribute DocumentosProdutorDTO documentos
    ) {
        try {
            Usuario salvo = usuarioService.cadastrarProdutor(dto, documentos);
            return ResponseEntity.ok(Map.of(
                "message", "Produtor cadastrado com sucesso! Documentos enviados para análise.",
                "idUsuario", salvo.getIdUsuario()
            ));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
 // --------------------------
 // Cadastro de Moderador
 // --------------------------
 @PostMapping("/moderador")
 public ResponseEntity<?> cadastrarModerador(@ModelAttribute CadastroUsuarioDTO dto) {
     try {
         Usuario salvo = usuarioService.cadastrarModerador(dto);
         return ResponseEntity.ok(Map.of(
             "message", "Moderador cadastrado com sucesso!",
             "idUsuario", salvo.getIdUsuario()
         ));
     } catch (IllegalArgumentException | IOException e) {
         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
     }
 }

}