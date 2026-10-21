package com.unla.epicentrogourmet.services.implementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.services.IEmailService;

@Service("emailService")
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviar(String destinatario, String asunto, String texto) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresá una dirección de correo electrónico.");
        }

        if (asunto == null || asunto.isBlank()) {
            throw new IllegalArgumentException(
                    "El asunto no puede estar vacío.");
        }

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto del correo no puede estar vacío.");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario.trim());
        mensaje.setSubject(asunto);
        mensaje.setText(texto);

        mailSender.send(mensaje);
    }
}