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

    private void sendEmail(String toEmail, String subject, String body) {
        System.out.println("=== EMAIL SERVICE ===");
        System.out.println("API Key presente: " + (sendGridApiKey != null && !sendGridApiKey.isBlank()));
        System.out.println("API Key empieza con: " + (sendGridApiKey != null && sendGridApiKey.length() > 5 ? sendGridApiKey.substring(0, 5) : "NULL_O_CORTA"));
        System.out.println("Enviando a: " + toEmail);
        System.out.println("Desde: " + fromEmail);

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("=== SENDGRID RESPONSE ===");
            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());
        } catch (IOException e) {
            System.out.println("=== SENDGRID ERROR ===");
            System.out.println("Error: " + e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        System.out.println("=== FORGOT PASSWORD para: " + toEmail);
        String resetLink = frontendUrl + "/?resetToken=" + token;
        sendEmail(toEmail,
                "Restablecer contraseña - Codelingo",
                "Hola!\n\nRecibimos una solicitud para restablecer tu contraseña en Codelingo.\n\n" +
                        "Hacé clic en el siguiente enlace:\n" + resetLink + "\n\n" +
                        "Este enlace expira en 1 hora.\n\nEl equipo de Codelingo"
        );
    }

    public void sendVerificationEmail(String toEmail, String token) {
        System.out.println("=== VERIFICATION EMAIL para: " + toEmail);
        String verifyLink = frontendUrl + "/?verifyToken=" + token;
        sendEmail(toEmail,
                "Verificá tu cuenta - Codelingo",
                "Hola!\n\nGracias por registrarte en Codelingo.\n\n" +
                        "Hacé clic en el siguiente enlace para verificar tu cuenta:\n" + verifyLink + "\n\n" +
                        "Este enlace expira en 24 horas.\n\nEl equipo de Codelingo"
        );
    }
}