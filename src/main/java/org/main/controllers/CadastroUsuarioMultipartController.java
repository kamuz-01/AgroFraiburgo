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
public class CadastroUsuarioMultipartController {

    private final UsuarioService usuarioService;

    public CadastroUsuarioMultipartController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastro-multipart")
    public ResponseEntity<?> cadastrarUsuarioMultipart(
            @Valid @ModelAttribute CadastroUsuarioDTO dto,
            @ModelAttribute DocumentosProdutorDTO documentos,
            BindingResult result) {

        if (result.hasErrors()) {
            Map<String, String> erros = new HashMap<>();
            result.getFieldErrors().forEach(err -> erros.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(erros);
        }

        try {
            Usuario usuario;
            if (dto.getTipoUsuario() == TipoUsuario.PRODUTOR) {
                // Salva produtor já com documentos + imagens
                usuario = usuarioService.cadastrarProdutor(dto, documentos);
                return ResponseEntity.ok(Map.of(
                    "message", "Produtor cadastrado com sucesso! Seu cadastro está pendente de aprovação e seus documentos serão avaliados.",
                    "id", usuario.getIdUsuario(),
                    "isProdutor", true
                ));
            }else if (dto.getTipoUsuario() == TipoUsuario.MODERADOR) {
                usuario = usuarioService.cadastrarModerador(dto);
                return ResponseEntity.ok(Map.of(
                    "message", "Moderador cadastrado com sucesso!",
                    "id", usuario.getIdUsuario(),
                    "isProdutor", false
                ));
            } else {
                usuario = usuarioService.cadastrarConsumidor(dto);
                return ResponseEntity.ok(Map.of(
                    "message", "Usuário cadastrado com sucesso!",
                    "id", usuario.getIdUsuario(),
                    "isProdutor", false
                ));
            }
        } catch (MultipartException e) {
            return ResponseEntity.badRequest().body("Erro no upload de arquivos: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }
}