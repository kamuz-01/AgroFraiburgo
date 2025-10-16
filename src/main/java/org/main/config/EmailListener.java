package org.main.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.main.DTOs.EmailMensagens;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void processEmail(EmailMensagens msg) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setTo(msg.getTo());
            helper.setSubject(msg.getSubject());
            helper.setText(msg.getBody(), true); // true = HTML
            helper.setFrom("no-reply@seu-dominio.com"); // ou use spring.mail.from

            mailSender.send(mime);
            log.info("Email enviado para {}", msg.getTo());
        } catch (MessagingException ex) {
            log.error("Falha ao enviar email para {}: {}", msg.getTo(), ex.getMessage(), ex);
            // opcional: requeue / dead-letter / persist log para auditoria
        } catch (Exception ex) {
            log.error("Erro inesperado ao processar email: {}", ex.getMessage(), ex);
        }
    }
}