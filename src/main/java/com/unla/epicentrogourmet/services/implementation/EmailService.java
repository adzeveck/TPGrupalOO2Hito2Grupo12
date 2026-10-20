
package com.unla.epicentrogourmet.services.implementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.services.IEmailService;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

@Service("emailService")
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void enviarCorreoPrueba(String destinatario) {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException(
                    "Ingresá una dirección de correo electrónico.");
        }

        String correo = destinatario.trim();

        try {
            InternetAddress direccion = new InternetAddress(correo);
            direccion.validate();
        } catch (AddressException e) {
            throw new IllegalArgumentException(
                    "La dirección de correo electrónico no es válida.");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Prueba de correo - Epicentro Gourmet");
        mensaje.setText(
                "Este es un correo de prueba enviado desde Epicentro Gourmet.");

        mailSender.send(mensaje);
    }
}
