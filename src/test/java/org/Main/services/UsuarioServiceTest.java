package org.Main.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.main.DTOs.CadastroUsuarioDTO;
import org.main.DTOs.DocumentosProdutorDTO;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.EmailBoasVindasService;
import org.main.services.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        EmailBoasVindasService emailBoasVindasService = mock(EmailBoasVindasService.class);
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, emailBoasVindasService);
    }

    @Test
    void atualizarPerfilUsuarioDeveRejeitarTelefoneComMenosDeDezDigitos() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.atualizarPerfilUsuario(
                1, null, null, null, null, "123456789", null, null, null,
                null, false, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Telefone deve conter 10 ou 11 dígitos numéricos.");

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void atualizarPerfilUsuarioDeveRejeitarTelefoneComMaisDeOnzeDigitos() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.atualizarPerfilUsuario(
                1, null, null, null, null, "123456789012", null, null, null,
                null, false, null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Telefone deve conter 10 ou 11 dígitos numéricos.");

        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void atualizarPerfilUsuarioDeveNormalizarTelefoneValidoAntesDeSalvar() throws Exception {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario atualizado = usuarioService.atualizarPerfilUsuario(
                1, null, null, null, null, "(49) 99999-0000", null, null, null,
                null, false, null, false);

        assertThat(atualizado.getTelefone()).isEqualTo("49999990000");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cadastrarProdutorDeveRejeitarEmailDuplicadoAntesDeSalvar() {
        CadastroUsuarioDTO dto = cadastroProdutor();
        when(usuarioRepository.existsByEmail("produtor@email.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrarProdutor(dto, new DocumentosProdutorDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já cadastrado.");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cadastrarProdutorDeveRejeitarNomeLoginDuplicadoAntesDeSalvar() {
        CadastroUsuarioDTO dto = cadastroProdutor();
        when(usuarioRepository.existsByEmail("produtor@email.com")).thenReturn(false);
        when(usuarioRepository.existsByNomeLogin("produtor")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrarProdutor(dto, new DocumentosProdutorDTO()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome de login já cadastrado.");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    private CadastroUsuarioDTO cadastroProdutor() {
        CadastroUsuarioDTO dto = new CadastroUsuarioDTO();
        dto.setTipoUsuario(TipoUsuario.PRODUTOR);
        dto.setEmail("produtor@email.com");
        dto.setNomeLogin("produtor");
        return dto;
    }
}
