package org.main.services;

import java.util.List;
import org.main.DTOs.NotificacaoModeradorDTO;
import org.main.enums.StatusConta;
import org.main.enums.TipoUsuario;
import org.main.models.Usuario;
import org.main.repository.UsuarioRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ModeradoresService {

    private final UsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${fila.bloqueios}") // configurada no application.properties
    private String filaBloqueios;

    public ModeradoresService(UsuarioRepository usuarioRepository, RabbitTemplate rabbitTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Lista todos os usuários e converte cada entidade Usuario para UsuarioDTO.
     */
    public List<Usuario> listarTodosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getTipoUsuario() != TipoUsuario.MODERADOR) // exclui moderadores
                .toList();
    }

    /**
     * Altera o status do usuário (entidade) e envia notificação para RabbitMQ.
     */
    public void alterarStatus(Integer id, StatusConta novoStatus, String observacao) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setStatusConta(novoStatus);
        usuarioRepository.save(usuario);

        // Monta a notificação para enviar ao RabbitMQ
        NotificacaoModeradorDTO notificacao = new NotificacaoModeradorDTO(
                usuario.getEmail(),
                usuario.getNome(),
                novoStatus.name(),
                observacao
        );

        rabbitTemplate.convertAndSend("fila_notificacoes", notificacao);
    }
    
    public void enviarMensagemRabbit(String mensagem) {
        rabbitTemplate.convertAndSend(filaBloqueios, mensagem);
    }

    /**
     * Mapeador entidade -> DTO (ajuste conforme campos reais do seu DTO).
     */
    /*private UsuarioDTO toDto(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(u.getIdUsuario());
        dto.setNome(u.getNome());
        dto.setSobrenome(u.getSobrenome());
        dto.setCpf(u.getCpf());
        dto.setSexo(u.getSexo());
        // se seu DTO possuir enums semelhantes:
        dto.setTipoUsuario(u.getTipoUsuario());
        dto.setCriadoEm(u.getCriadoEm());
        dto.setCidade(u.getCidade());
        dto.setEstado(u.getEstado());
        dto.setStatusConta(u.getStatusConta());
        return dto;
    }*/
}