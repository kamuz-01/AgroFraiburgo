package org.main.controllers;

import org.main.models.Feira;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.FeiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderador/feiras")
public class FeiraController {

    private final FeiraService feiraService;
    private final UsuarioRepository usuarioRepository;

    public FeiraController(FeiraService feiraService, UsuarioRepository usuarioRepository) {
        this.feiraService = feiraService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Feira>> listarTodas() {
        return ResponseEntity.ok(feiraService.listarTodas());
    }

    @PostMapping
    public ResponseEntity<Feira> criar(@RequestBody Feira feira, Authentication auth) {
        // auth.getName() retorna o ID do usuário logado
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        feira.setModerador(usuario);
        Feira nova = feiraService.salvar(feira);
        return ResponseEntity.ok(nova);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feira> atualizar(@PathVariable Integer id,
                                           @RequestBody Feira feiraAtualizada,
                                           Authentication auth) {
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        feiraAtualizada.setModerador(usuario);
        Feira feira = feiraService.atualizar(id, feiraAtualizada);
        return ResponseEntity.ok(feira);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feira> buscarFeiraPorId(@PathVariable Integer id, Authentication auth) {
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        Feira feira = feiraService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Feira não encontrada"));

        return ResponseEntity.ok(feira);
    }
}