
package com.unla.epicentrogourmet.controllers;

import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IEmailService;

@Controller
@RequestMapping("/admin/email-test")
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {

    private final IEmailService emailService;

    public EmailController(IEmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ModelAndView mostrarFormulario() {
        return new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);
    }

    @PostMapping
    public ModelAndView enviarCorreo(
            @RequestParam("destinatario") String destinatario) {

        ModelAndView vista =
                new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);

        vista.addObject("destinatario", destinatario);

        try {
            emailService.enviarCorreoPrueba(destinatario);
            vista.addObject("mensajeExito",
                    "El correo se envió correctamente.");
        } catch (IllegalArgumentException e) {
            vista.addObject("mensajeError", e.getMessage());
        } catch (MailException e) {
            vista.addObject("mensajeError",
                    "No se pudo enviar el correo. Revisá la configuración "
                            + "SMTP y las credenciales.");
        }

        return vista;
    }
}
