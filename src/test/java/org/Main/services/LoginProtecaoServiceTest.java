package org.Main.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.main.enums.LoginBloqueioEtapa;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.main.services.LoginProtecaoService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginProtecaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LoginProtecaoService loginProtecaoService;

    @Test
    void primeiraFalhaNaoDeveExibirAvisoNemBloqueio() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<LoginProtecaoService.LoginFalhaResultado> resultado = loginProtecaoService.registrarFalha("joao");

        assertThat(resultado).isEmpty();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginFalhasConsecutivas()).isEqualTo(1);
        assertThat(captor.getValue().getLoginBloqueioEtapa()).isEqualTo(LoginBloqueioEtapa.LIVRE);
    }

    @Test
    void segundaFalhaDeveAvisarSobreProximoBloqueio() {
        Usuario usuario = usuarioAtivo();
        usuario.setLoginFalhasConsecutivas(1);
        when(usuarioRepository.findByNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<LoginProtecaoService.LoginFalhaResultado> resultado = loginProtecaoService.registrarFalha("joao");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().aviso()).isTrue();
        assertThat(resultado.get().bloqueado()).isFalse();
        assertThat(resultado.get().mensagem()).contains("segunda tentativa falha consecutiva");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginFalhasConsecutivas()).isEqualTo(2);
    }

    @Test
    void terceiraFalhaDeveBloquearPorDozeHoras() {
        Usuario usuario = usuarioAtivo();
        usuario.setLoginFalhasConsecutivas(2);
        when(usuarioRepository.findByNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<LoginProtecaoService.LoginFalhaResultado> resultado = loginProtecaoService.registrarFalha("joao");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().bloqueado()).isTrue();
        assertThat(resultado.get().mensagem()).contains("12 horas");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginBloqueioEtapa()).isEqualTo(LoginBloqueioEtapa.BLOQUEIO_12H);
        assertThat(captor.getValue().getLoginFalhasConsecutivas()).isZero();
        assertThat(captor.getValue().getLoginBloqueadoAte()).isAfter(LocalDateTime.now());
    }

    @Test
    void terceiraFalhaDepoisDoBloqueioDeDozeHorasDeveBloquearPorVinteQuatroHoras() {
        Usuario usuario = usuarioAtivo();
        usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_12H);
        usuario.setLoginFalhasConsecutivas(2);
        usuario.setLoginBloqueadoAte(LocalDateTime.now().minusHours(1));

        when(usuarioRepository.findByNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<LoginProtecaoService.LoginFalhaResultado> resultado = loginProtecaoService.registrarFalha("joao");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().bloqueado()).isTrue();
        assertThat(resultado.get().mensagem()).contains("24 horas");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginBloqueioEtapa()).isEqualTo(LoginBloqueioEtapa.BLOQUEIO_24H);
        assertThat(captor.getValue().getLoginFalhasConsecutivas()).isZero();
        assertThat(captor.getValue().getLoginBloqueadoAte()).isAfter(LocalDateTime.now());
    }

    @Test
    void terceiraFalhaDepoisDoBloqueioDeVinteQuatroHorasDeveGerarBanimentoDefinitivo() {
        Usuario usuario = usuarioAtivo();
        usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_24H);
        usuario.setLoginFalhasConsecutivas(2);
        usuario.setLoginBloqueadoAte(LocalDateTime.now().minusHours(1));

        when(usuarioRepository.findByNomeLogin("joao")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<LoginProtecaoService.LoginFalhaResultado> resultado = loginProtecaoService.registrarFalha("joao");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().bloqueado()).isTrue();
        assertThat(resultado.get().mensagem()).contains("definitivamente");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getLoginBloqueioEtapa()).isEqualTo(LoginBloqueioEtapa.BLOQUEIO_DEFINITIVO);
        assertThat(captor.getValue().getStatusConta()).isEqualTo(StatusConta.BLOQUEADO);
        assertThat(captor.getValue().getLoginBloqueadoAte()).isNull();
    }

    @Test
    void registrarSucessoDeveLimparFalhasEVoltarOEstagioParaLivre() {
        Usuario usuario = usuarioAtivo();
        usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.BLOQUEIO_12H);
        usuario.setLoginFalhasConsecutivas(2);
        usuario.setLoginBloqueadoAte(LocalDateTime.now().minusHours(1));

        loginProtecaoService.registrarSucesso(usuario);

        assertThat(usuario.getLoginBloqueioEtapa()).isEqualTo(LoginBloqueioEtapa.LIVRE);
        assertThat(usuario.getLoginFalhasConsecutivas()).isZero();
        assertThat(usuario.getLoginBloqueadoAte()).isNull();
        verify(usuarioRepository).save(usuario);
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNomeLogin("joao");
        usuario.setSenha("senha");
        usuario.setTipoUsuario(TipoUsuario.CONSUMIDOR);
        usuario.setStatusConta(StatusConta.ATIVO);
        usuario.setLoginBloqueioEtapa(LoginBloqueioEtapa.LIVRE);
        usuario.setLoginFalhasConsecutivas(0);
        return usuario;
    }
}
