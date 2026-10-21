
package com.unla.epicentrogourmet.controllers;

import org.springframework.mail.MailException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
@RequestMapping("/admin/emailTest")
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {

    private final IEmailService emailService;

    public EmailController(IEmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public ModelAndView mostrarFormulario(
            @RequestParam(value = "enviado", required = false) Boolean enviado) {

        ModelAndView vista =
                new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);

        if (Boolean.TRUE.equals(enviado)) {
            vista.addObject("mensajeExito",
                    "El correo se envió correctamente.");
        }

        return vista;
    }

    @PostMapping
    public ModelAndView enviarCorreo(
            @RequestParam("destinatario") String destinatario,
            RedirectAttributes redirectAttributes) {

        try {
            emailService.enviar(
                    destinatario,
                    "Prueba de correo - Epicentro Gourmet",
                    "Este es un correo de prueba enviado desde Epicentro Gourmet."
            );

            redirectAttributes.addAttribute("enviado", true);

            return new ModelAndView("redirect:/admin/email-test");

        } catch (IllegalArgumentException e) {
            ModelAndView vista =
                    new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);

            vista.addObject("destinatario", destinatario);
            vista.addObject("mensajeError", e.getMessage());

            return vista;

        } catch (org.springframework.mail.MailException e) {
            org.slf4j.LoggerFactory.getLogger(EmailController.class)
                    .error("Error al enviar el correo de prueba", e);

            ModelAndView vista =
                    new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);

            vista.addObject("destinatario", destinatario);
            vista.addObject("mensajeError",
                    "No se pudo enviar el correo. Revisá la configuración SMTP.");

            return vista;
        }
    }
}
