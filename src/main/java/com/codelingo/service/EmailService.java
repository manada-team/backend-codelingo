package com.codelingo.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${mail.from:codelingo.ua@gmail.com}")
    private String fromEmail;

    @Value("${codelingo.frontend.url:https://codelingo-tau.vercel.app}")
    private String frontendUrl;

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
        } catch (IOException e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/?resetToken=" + token;
        String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto'>" +
                "<h2 style='color:#333'>Restablecer contraseña</h2>" +
                "<p>Recibimos una solicitud para restablecer tu contraseña en <strong>Codelingo</strong>.</p>" +
                "<p style='text-align:center;margin:32px 0'>" +
                "<a href='" + resetLink + "' style='background:#e91e8c;color:#fff;padding:12px 28px;" +
                "border-radius:6px;text-decoration:none;font-weight:bold'>Restablecer contraseña</a></p>" +
                "<p style='color:#888;font-size:13px'>Este enlace expira en 1 hora. Si no solicitaste esto, ignorá este correo.</p>" +
                "<p style='color:#aaa;font-size:12px'>— El equipo de Codelingo</p></div>";
        sendEmail(toEmail, "Restablecer contraseña - Codelingo", html);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/?verifyToken=" + token;
        String html = "<div style='font-family:sans-serif;max-width:480px;margin:auto'>" +
                "<h2 style='color:#333'>Verificá tu cuenta</h2>" +
                "<p>Gracias por registrarte en <strong>Codelingo</strong>. Hacé clic para activar tu cuenta.</p>" +
                "<p style='text-align:center;margin:32px 0'>" +
                "<a href='" + verifyLink + "' style='background:#e91e8c;color:#fff;padding:12px 28px;" +
                "border-radius:6px;text-decoration:none;font-weight:bold'>Verificar cuenta</a></p>" +
                "<p style='color:#888;font-size:13px'>Este enlace expira en 24 horas.</p>" +
                "<p style='color:#aaa;font-size:12px'>— El equipo de Codelingo</p></div>";
        sendEmail(toEmail, "Verificar cuenta - Codelingo", html);
    }
}