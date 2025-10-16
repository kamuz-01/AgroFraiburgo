package org.main.services;

import lombok.RequiredArgsConstructor;
import org.main.DTOs.EmailMensagens;
import org.main.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RabbitTemplate rabbitTemplate;
    private final JavaMailSender mailSender;

    /**
     * Enfileira a mensagem para envio de e-mail (assíncrono via RabbitMQ)
     */
    public void enqueueEmail(String to, String subject, String bodyHtml) {
        EmailMensagens msg = new EmailMensagens(to, subject, bodyHtml);
        rabbitTemplate.convertAndSend(RabbitConfig.EMAIL_EXCHANGE, RabbitConfig.EMAIL_ROUTING_KEY, msg);
    }
    
    public void enviarEmail(String destinatario, String assunto, String corpo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, false); // false = texto simples (pode mudar para true se for HTML)
            helper.setFrom("no-reply@seudominio.com"); // ou configure spring.mail.from

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail: " + e.getMessage(), e);
        }
    }
}