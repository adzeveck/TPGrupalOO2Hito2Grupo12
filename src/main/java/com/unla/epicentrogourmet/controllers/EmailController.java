package com.unla.epicentrogourmet.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

// Prueba de envío de e-mail: el admin escribe un destinatario y el sistema le manda un correo fijo
@Controller
@RequestMapping("/admin/emailTest")
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {

	private static final Logger log = LoggerFactory.getLogger(EmailController.class);

	private final IEmailService emailService;

	public EmailController(IEmailService emailService) {
		this.emailService = emailService;
	}

	// GET /admin/emailTest (con ?enviado después de un envío exitoso)
	@GetMapping
	public ModelAndView mostrarFormulario(@RequestParam(value = "enviado", required = false) String enviado) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);
		if (enviado != null) {
			modelAndView.addObject("mensajeExito", "El correo se envió correctamente.");
		}
		return modelAndView;
	}

	// POST /admin/emailTest
	@PostMapping
	public ModelAndView enviarCorreo(@RequestParam("destinatario") String destinatario) {
		try {
			emailService.enviar(destinatario, "Prueba de correo - Epicentro Gourmet",
					"Este es un correo de prueba enviado desde Epicentro Gourmet.");
			// POST-Redirect-GET: si se recarga la página no se vuelve a mandar el correo
			return new ModelAndView("redirect:" + ViewRouteHelper.EMAIL_TEST_ROOT + "?enviado");
		} catch (IllegalArgumentException e) {
			return formConError(destinatario, e.getMessage());
		} catch (MailException e) {
			log.error("Error al enviar el correo de prueba", e);
			return formConError(destinatario, "No se pudo enviar el correo. Revisá la configuración SMTP.");
		}
	}

	private ModelAndView formConError(String destinatario, String mensaje) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMAIL_TEST_FORM);
		modelAndView.addObject("destinatario", destinatario);
		modelAndView.addObject("mensajeError", mensaje);
		return modelAndView;
	}
}
