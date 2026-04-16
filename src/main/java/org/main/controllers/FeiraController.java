package org.main.controllers;

import org.main.DTOs.FeiraDTO;
import org.main.exceptions.FeiraNaoEncontradaException;
import org.main.exceptions.UsuarioNaoEncontradoException;
import org.main.models.Feira;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.FeiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

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
    public ResponseEntity<List<FeiraDTO>> listarTodas() {
        return ResponseEntity.ok(feiraService.listarTodas().stream()
                .map(FeiraDTO::fromEntity)
                .toList());
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody FeiraDTO feiraDto, Authentication auth) {
        // auth.getName() retorna o ID do usuário logado
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(UsuarioNaoEncontradoException::new);

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        Feira feira = toEntity(feiraDto);
        feira.setModerador(usuario);
        Feira nova = feiraService.salvar(feira);
        return ResponseEntity.ok(FeiraDTO.fromEntity(nova));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id,
                                           @Valid @RequestBody FeiraDTO feiraDto,
                                           Authentication auth) {
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(UsuarioNaoEncontradoException::new);

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        Feira feiraAtualizada = toEntity(feiraDto);
        feiraAtualizada.setModerador(usuario);
        Feira feira = feiraService.atualizar(id, feiraAtualizada);
        return ResponseEntity.ok(FeiraDTO.fromEntity(feira));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarFeiraPorId(@PathVariable Integer id, Authentication auth) {
        Integer userId = Integer.valueOf(auth.getName());

        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(UsuarioNaoEncontradoException::new);

        if (!usuario.getTipoUsuario().name().equals("MODERADOR")) {
            return ResponseEntity.status(403).build();
        }

        Feira feira = feiraService.buscarPorId(id)
            .orElseThrow(FeiraNaoEncontradaException::new);

        return ResponseEntity.ok(FeiraDTO.fromEntity(feira));
    }

    private Feira toEntity(FeiraDTO dto) {
        Feira feira = new Feira();
        feira.setNomeLocal(dto.getNomeLocal());
        feira.setLogradouro(dto.getLogradouro());
        feira.setNumero(dto.getNumero());
        feira.setBairro(dto.getBairro());
        feira.setComplemento(dto.getComplemento());
        feira.setStatusFeira(dto.getStatusFeira());
        return feira;
    }
}