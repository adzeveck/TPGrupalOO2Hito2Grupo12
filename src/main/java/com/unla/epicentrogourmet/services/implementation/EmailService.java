package com.unla.epicentrogourmet.services.implementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String destinatario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(destinatario);
        mensaje.setSubject("Prueba de envío - Epicentro Gourmet");
        mensaje.setText(
                "Este es un correo de prueba de Epicentro Gourmet.\n\n"
                        + "El servicio de envío de emails funciona correctamente."
        );

        mailSender.send(mensaje);
    }
}
