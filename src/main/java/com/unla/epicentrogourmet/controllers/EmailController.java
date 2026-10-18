package com.unla.epicentrogourmet.controllers;

import com.unla.epicentrogourmet.services.implementation.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<String> enviarEmail(
            @RequestParam String destinatario) {

        emailService.enviarEmail(destinatario);

        return ResponseEntity.ok(
                "El correo fue enviado correctamente a " + destinatario
        );
    }
}
