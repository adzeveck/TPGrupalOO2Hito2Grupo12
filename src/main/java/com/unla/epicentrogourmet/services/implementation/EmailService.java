
package com.unla.epicentrogourmet.services.implementation;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String destinatario) {
        if (!StringUtils.hasText(destinatario)
                || !destinatario.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Ingresá una dirección de correo válida."
            );
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(destinatario);
        mensaje.setSubject("Prueba de envío - Epicentro Gourmet");
        mensaje.setText(
                "Hola,\n\n"
                        + "Este es un correo de prueba de Epicentro Gourmet.\n"
                        + "El servicio de envío de emails funciona correctamente."
        );

        mailSender.send(mensaje);
    }
}

