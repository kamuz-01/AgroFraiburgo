package org.main.services;

import org.main.models.Usuario;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailBoasVindasService {

    private final EmailService emailService;

    /**
     * Envia email de boas-vindas personalizado conforme o tipo de usuário
     */
    public void enviarEmailBoasVindas(Usuario usuario) {
        String assunto;
        String corpo;

        switch (usuario.getTipoUsuario()) {
            case CONSUMIDOR -> {
                assunto = "Bem-vindo(a) à AgroFraiburgo! 🌱";
                corpo = construirEmailConsumidor(usuario);
            }
            case PRODUTOR -> {
                assunto = "Cadastro em Análise - AgroFraiburgo 📋";
                corpo = construirEmailProdutor(usuario);
            }
            case MODERADOR -> {
                assunto = "Bem-vindo(a) ao Time de Moderação! 🛡️";
                corpo = construirEmailModerador(usuario);
            }
            default -> {
                assunto = "Cadastro Realizado - AgroFraiburgo";
                corpo = construirEmailPadrao(usuario);
            }
        }

        // Enfileira o email para envio assíncrono via RabbitMQ
        emailService.enqueueEmail(usuario.getEmail(), assunto, corpo);
    }

    /**
     * Template de email para CONSUMIDOR
     */
    private String construirEmailConsumidor(Usuario usuario) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #ffffff;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 30px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%);
                        color: black;
                        padding: 20px 20px;
                        text-align: center;
                        margin-top: 30px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .header p {
                        margin: 10px 0 0 0;
                        font-size: 16px;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333;
                        line-height: 1.6;
                    }
                    .content h2 {
                        color: #4CAF50;
                        font-size: 22px;
                        margin-bottom: 15px;
                    }
                    .welcome-box {
                        background-color: #e8f5e9;
                        border-left: 4px solid #4CAF50;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .benefits {
                        margin: 25px 0;
                    }
                    .benefit-item {
                        display: flex;
                        align-items: flex-start;
                        margin: 15px 0;
                    }
                    .benefit-icon {
                        font-size: 24px;
                        margin-right: 15px;
                        color: #4CAF50;
                    }
                    .cta-button {
                        display: inline-block;
                        background-color: #4CAF50;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 14px;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌱 AgroFraiburgo</h1>
                        <p>Conectando você aos melhores produtores locais</p>
                    </div>
                    
                    <div class="content">
                        <div class="welcome-box">
                            <h2>Olá, %s! 👋</h2>
                            <p>É com grande alegria que damos as boas-vindas à <strong>AgroFraiburgo</strong>!</p>
                        </div>
                        
                        <p>Seu cadastro foi realizado com sucesso e você já pode aproveitar tudo o que nossa plataforma tem a oferecer.</p>
                        
                        <h2>O que você pode fazer agora:</h2>
                        
                        <div class="benefits">
                            <div class="benefit-item">
                                <span class="benefit-icon">🛒</span>
                                <div>
                                    <strong>Explorar Produtos Frescos</strong>
                                    <p style="margin: 5px 0 0 0;">Navegue por centenas de produtos orgânicos e da agricultura familiar.</p>
                                </div>
                            </div>
                            
                            <div class="benefit-item">
                                <span class="benefit-icon">👨‍🌾</span>
                                <div>
                                    <strong>Conhecer os Produtores</strong>
                                    <p style="margin: 5px 0 0 0;">Descubra quem cultiva seus alimentos e apoie produtores locais.</p>
                                </div>
                            </div>
                            
                            <div class="benefit-item">
                                <span class="benefit-icon">📍</span>
                                <div>
                                    <strong>Localizar Feiras</strong>
                                    <p style="margin: 5px 0 0 0;">Encontre as feiras mais próximas de você e compre direto do produtor.</p>
                                </div>
                            </div>
                            
                            <div class="benefit-item">
                                <span class="benefit-icon">⭐</span>
                                <div>
                                    <strong>Avaliar e Compartilhar</strong>
                                    <p style="margin: 5px 0 0 0;">Ajude outros consumidores com suas avaliações e experiências.</p>
                                </div>
                            </div>
                        </div>
                        
                        <div style="text-align: center; margin-top: 30px;">
                            <a href="http://localhost:8080/produtos" class="cta-button">Começar a Explorar</a>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p><strong>AgroFraiburgo</strong> - Agricultura Sustentável e Comércio Justo</p>
                        <p>Dúvidas? Entre em contato: <a href="mailto:suporte@agrofraiburgo.com.br">suporte@agrofraiburgo.com.br</a></p>
                        <p style="margin-top: 15px; font-size: 12px; color: #999;">
                            Este é um email automático. Por favor, não responda.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, usuario.getNome());
    }

    /**
     * Template de email para PRODUTOR
     */
    private String construirEmailProdutor(Usuario usuario) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #ffffff;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 30px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #FF9800 0%%, #F57C00 100%%);
                        color: black;
                        padding: 20px 20px;
                        text-align: center;
                        margin-top: 30px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333;
                        line-height: 1.6;
                    }
                    .status-box {
                        background-color: #fff3e0;
                        border-left: 4px solid #FF9800;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .status-box h2 {
                        color: #F57C00;
                        margin: 0 0 10px 0;
                        font-size: 20px;
                    }
                    .info-section {
                        background-color: #f9f9f9;
                        padding: 20px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .info-section h3 {
                        color: #FF9800;
                        margin-top: 0;
                        font-size: 18px;
                    }
                    .checklist {
                        list-style: none;
                        padding: 0;
                    }
                    .checklist li {
                        padding: 10px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .checklist li:last-child {
                        border-bottom: none;
                    }
                    .checklist li:before {
                        content: "✓ ";
                        color: #4CAF50;
                        font-weight: bold;
                        margin-right: 10px;
                    }
                    .timeline {
                        margin: 20px 0;
                        padding-left: 20px;
                        border-left: 3px solid #FF9800;
                    }
                    .timeline-item {
                        margin: 15px 0;
                        padding-left: 15px;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 14px;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📋 Cadastro em Análise</h1>
                        <p>AgroFraiburgo - Plataforma de Produtores</p>
                    </div>
                    
                    <div class="content">
                        <h2>Olá, %s! 👨‍🌾</h2>
                        
                        <p>Obrigado por se cadastrar como <strong>produtor</strong> na AgroFraiburgo!</p>
                        
                        <div class="status-box">
                            <h2>⏳ Status: Em Análise</h2>
                            <p>Recebemos seu cadastro e seus documentos estão sendo analisados por nossa equipe de moderação.</p>
                        </div>
                        
                        <div class="info-section">
                            <h3>O que acontece agora?</h3>
                            
                            <div class="timeline">
                                <div class="timeline-item">
                                    <strong>1. Verificação de Documentos</strong>
                                    <p style="margin: 5px 0; color: #666;">Nossa equipe está verificando todos os documentos enviados.</p>
                                </div>
                                
                                <div class="timeline-item">
                                    <strong>2. Análise de Conformidade</strong>
                                    <p style="margin: 5px 0; color: #666;">Verificamos se tudo está de acordo com os requisitos da plataforma.</p>
                                </div>
                                
                                <div class="timeline-item">
                                    <strong>3. Aprovação ou Feedback</strong>
                                    <p style="margin: 5px 0; color: #666;">Você receberá um email com o resultado em até 3-5 dias úteis.</p>
                                </div>
                            </div>
                        </div>
                        
                        <div class="info-section">
                            <h3>Documentos Recebidos:</h3>
                            <ul class="checklist">
                                <li>Documento de Identidade</li>
                                <li>Comprovante de Residência</li>
                                <li>Declaração de aptidão ao PRONAF (DAP)</li>
                                <li>Certificado de Produção Orgânica</li>
                                <li>Código de rastreabilidade (para frutas, legumes e verduras)</li>
                                <li>Número de inscrição Estadual</li>
                                <li>Alvará Sanitário</li>
                            </ul>
                        </div>
                        
                        <div style="background-color: #e3f2fd; border-left: 4px solid #2196F3; padding: 15px; margin: 20px 0; border-radius: 5px;">
                            <p style="margin: 0;">
                                <strong>📧 Fique Atento:</strong> Enviaremos todas as atualizações para o email <strong>%s</strong>. 
                                Verifique também sua caixa de spam!
                            </p>
                        </div>
                        
                        <p style="margin-top: 30px;">
                            <strong>Importante:</strong> Enquanto sua conta não for aprovada, você não poderá acessar as funcionalidades de produtor na plataforma.
                        </p>
                        
                        <p style="margin-top: 20px; color: #666; font-size: 14px;">
                            Se tiver dúvidas sobre o processo de análise ou precisar enviar documentos adicionais, 
                            entre em contato conosco.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>AgroFraiburgo</strong> - Equipe de Moderação</p>
                        <p>Dúvidas? Entre em contato: <a href="mailto:moderacao@agrofraiburgo.com.br">moderacao@agrofraiburgo.com.br</a></p>
                        <p style="margin-top: 15px; font-size: 12px; color: #999;">
                            Este é um email automático. Por favor, não responda.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, usuario.getNome(), usuario.getEmail());
    }

    /**
     * Template de email para MODERADOR
     */
    private String construirEmailModerador(Usuario usuario) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #ffffff;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 30px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #3F51B5 0%%, #303F9F 100%%);
                        color: black;
                        padding: 20px 20px;
                        text-align: center;
                        margin-top: 30px;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 20px 30px;
                        color: #333;
                        line-height: 1.6;
                    }
                    .welcome-box {
                        background-color: #b3dbf2ff;
                        border-left: 4px solid #3F51B5;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .welcome-box h2 {
                        color: #3F51B5;
                        margin: 0 0 10px 0;
                    }
                    .responsibilities {
                        background-color: #f9f9f9;
                        padding: 20px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .responsibilities h3 {
                        color: #3F51B5;
                        margin-top: 0;
                    }
                    .responsibility-item {
                        display: flex;
                        align-items: flex-start;
                        margin: 15px 0;
                        padding: 10px;
                        background-color: white;
                        border-radius: 5px;
                    }
                    .responsibility-icon {
                        font-size: 24px;
                        margin-right: 15px;
                        color: #3F51B5;
                    }
                    .cta-button {
                        display: inline-block;
                        background-color: #3F51B5;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 14px;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛡️ Bem-vindo ao Time!</h1>
                        <p>AgroFraiburgo - Equipe de Moderação</p>
                    </div>
                    
                    <div class="content">
                        <div class="welcome-box">
                            <h2>Olá, %s! 🎉</h2>
                            <p><strong>Parabéns!</strong> Você agora faz parte da equipe de moderação da AgroFraiburgo.</p>
                        </div>
                        
                        <p>É com grande satisfação que damos as boas-vindas ao nosso time de moderadores. 
                        Sua função é fundamental para manter a qualidade e a confiança em nossa plataforma.</p>
                        
                        <div class="responsibilities">
                            <h3>Suas Responsabilidades:</h3>
                            
                            <div class="responsibility-item">
                                <span class="responsibility-icon">👨‍🌾</span>
                                <div>
                                    <strong>Análise de Cadastros de Produtores</strong>
                                    <p style="margin: 5px 0 0 0; color: #666;">
                                        Revisar e aprovar/rejeitar cadastros de novos produtores, verificando documentação e conformidade.
                                    </p>
                                </div>
                            </div>
                            
                            <div class="responsibility-item">
                                <span class="responsibility-icon">🏪</span>
                                <div>
                                    <strong>Gestão de Feiras</strong>
                                    <p style="margin: 5px 0 0 0; color: #666;">
                                        Cadastrar, atualizar e gerenciar informações sobre feiras e eventos.
                                    </p>
                                </div>
                            </div>
                            
                            <div class="responsibility-item">
                                <span class="responsibility-icon">⚖️</span>
                                <div>
                                    <strong>Moderação de Usuários</strong>
                                    <p style="margin: 5px 0 0 0; color: #666;">
                                        Gerenciar status de contas (ativar, bloquear, rejeitar) conforme as políticas da plataforma.
                                    </p>
                                </div>
                            </div>
                            
                            <div class="responsibility-item">
                                <span class="responsibility-icon">📧</span>
                                <div>
                                    <strong>Comunicação com Usuários</strong>
                                    <p style="margin: 5px 0 0 0; color: #666;">
                                        Enviar notificações e feedbacks sobre decisões de moderação.
                                    </p>
                                </div>
                            </div>
                        </div>
                        
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px;">
                            <p style="margin: 0;">
                                <strong>⚠️ Importante:</strong> Todas as suas ações como moderador são registradas no sistema. 
                                Mantenha sempre imparcialidade e profissionalismo em suas decisões.
                            </p>
                        </div>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="http://localhost:8080/home_moderador" class="cta-button">Acessar Painel de Moderação</a>
                        </div>
                        
                        <p style="margin-top: 30px; color: #666; font-size: 14px;">
                            💡 <strong>Dica:</strong> Familiarize-se com o painel de moderação e as ferramentas disponíveis. 
                            Em caso de dúvidas, entre em contato com o suporte de TI.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>AgroFraiburgo</strong> - Equipe de Administração</p>
                        <p>Dúvidas? Entre em contato: <a href="mailto:suporte.ti@agrofraiburgo.com.br">suporte.ti@agrofraiburgo.com.br</a></p>
                        <p style="margin-top: 15px; font-size: 12px; color: #999;">
                            Este é um email automático. Por favor, não responda.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, usuario.getNome());
    }

    /**
     * Template de email padrão (fallback)
     */
    private String construirEmailPadrao(Usuario usuario) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #4CAF50;">Olá, %s!</h2>
                    <p>Seu cadastro foi realizado com sucesso na plataforma AgroFraiburgo.</p>
                    <p>Em breve você receberá mais informações sobre como utilizar a plataforma.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 14px; color: #666;">
                        Atenciosamente,<br>
                        <strong>Equipe AgroFraiburgo</strong>
                    </p>
                </div>
            </body>
            </html>
            """, usuario.getNome());
    }
}