package com.codelingo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${codelingo.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/?resetToken=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Restablecer contraseña - Codelingo");
        message.setText(
                "Hola!\n\n" +
                        "Recibimos una solicitud para restablecer tu contraseña en Codelingo.\n\n" +
                        "Hacé clic en el siguiente enlace para crear una nueva contraseña:\n" +
                        resetLink + "\n\n" +
                        "Este enlace expira en 1 hora.\n\n" +
                        "Si no solicitaste esto, podés ignorar este email.\n\n" +
                        "El equipo de Codelingo"
        );

        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/?verifyToken=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verificá tu cuenta - Codelingo");
        message.setText(
                "Hola!\n\n" +
                        "Gracias por registrarte en Codelingo.\n\n" +
                        "Hacé clic en el siguiente enlace para verificar tu cuenta:\n" +
                        verifyLink + "\n\n" +
                        "Este enlace expira en 24 horas.\n\n" +
                        "El equipo de Codelingo"
        );

        mailSender.send(message);
    }
}