package org.Main.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.controllers.CadastroUsuarioMultipartController;
import org.main.enums.TipoUsuario;
import org.main.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CadastroUsuarioMultipartControllerTest {

    private UsuarioService usuarioService;
    private CadastroUsuarioMultipartController controller;

    @BeforeEach
    void setUp() {
        usuarioService = mock(UsuarioService.class);
        controller = new CadastroUsuarioMultipartController(usuarioService);
    }

    @Test
    void cadastroMultipartNaoDevePermitirCadastroPublicoDeModerador() throws Exception {
        CadastroUsuarioDTO dto = new CadastroUsuarioDTO();
        dto.setTipoUsuario(TipoUsuario.MODERADOR);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(dto, "dto");

        var response = controller.cadastrarUsuarioMultipart(dto, new DocumentosProdutorDTO(), bindingResult);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Cadastro de moderadores deve ser feito por um moderador autenticado.");
        verify(usuarioService, never()).cadastrarModerador(dto);
    }
}
