package org.main.config;

import org.main.DTOs.NotificacaoModerador;
import org.main.services.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificacaoConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "fila_notificacoes")
    public void receberMensagem(NotificacaoModerador notificacao) {
        String assunto;
        String corpo;

        switch (notificacao.getNovoStatus()) {
            case "ATIVO" -> {
                assunto = "Seu cadastro foi aprovado!";
                corpo = """
                        Olá %s,

                        Boa notícia 🎉 Seu cadastro foi aprovado e sua conta já está ativa.
                        Agora você pode acessar normalmente a plataforma.

                        Atenciosamente,
                        Equipe de Moderação
                        """.formatted(notificacao.getNome());
            }
            case "REJEITADO", "BLOQUEADO" -> {
                assunto = "Seu cadastro foi " + notificacao.getNovoStatus().toLowerCase();
                corpo = """
                        Olá %s,

                        Seu cadastro foi %s.

                        Motivo informado pelo moderador:
                        %s

                        Atenciosamente,
                        Equipe de Moderação
                        """.formatted(
                            notificacao.getNome(),
                            notificacao.getNovoStatus().toLowerCase(),
                            notificacao.getObservacao()
                        );
            }
            default -> {
                assunto = "Atualização do status da sua conta";
                corpo = "Olá %s, seu status mudou para: %s."
                        .formatted(notificacao.getNome(), notificacao.getNovoStatus());
            }
        }

        emailService.enviarEmail(notificacao.getEmail(), assunto, corpo);
    }
}