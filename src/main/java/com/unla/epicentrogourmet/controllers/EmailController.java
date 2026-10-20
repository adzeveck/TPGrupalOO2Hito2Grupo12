package com.unla.epicentrogourmet.controllers;

import com.unla.epicentrogourmet.services.implementation.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email-test")
    public String mostrarPaginaEmail() {
        return "emailTest";
    }

    @PostMapping("/api/test-email")
    @ResponseBody
    public ResponseEntity<String> enviarEmail(
            @RequestParam String destinatario) {

        try {
            emailService.enviarEmail(destinatario);

            return ResponseEntity.ok(
                    "Correo enviado correctamente a " + destinatario
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (MailException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("No se pudo enviar el correo. Revisá la configuración SMTP.");
        }
    }
}
