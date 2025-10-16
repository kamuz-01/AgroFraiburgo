package org.main.controllers;

import jakarta.validation.Valid;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class CadastroProdutorController {

    private final UsuarioService usuarioService;

    public CadastroProdutorController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro-produtor")
    public ResponseEntity<?> cadastrarProdutor(
            @Valid @ModelAttribute CadastroUsuarioDTO dto,
            @Valid @ModelAttribute DocumentosProdutorDTO documentos,
            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> erros = new HashMap<>();
            result.getFieldErrors().forEach(err -> erros.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(erros);
        }

        if (dto.getTipoUsuario() != TipoUsuario.PRODUTOR) {
            return ResponseEntity.badRequest().body(Map.of("error", "Este endpoint é apenas para produtores."));
        }

        try {
            Usuario usuario = usuarioService.cadastrarProdutor(dto, documentos);
            return ResponseEntity.ok(Map.of("message", "Produtor cadastrado com sucesso!", "id", usuario.getIdUsuario()));
        } catch (MultipartException e) {
            return ResponseEntity.badRequest().body("Erro no upload de arquivos: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao cadastrar produtor: " + e.getMessage());
        }
    }
}