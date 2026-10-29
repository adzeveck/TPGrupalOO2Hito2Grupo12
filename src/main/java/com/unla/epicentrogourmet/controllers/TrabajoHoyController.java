package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ITrabajoHoyService;

// El empleado consulta si trabaja hoy: se resuelve con el username del usuario logueado,
// así que nadie puede consultar por otro empleado
@Controller
@RequestMapping("/empleado/hoy")
@PreAuthorize("hasRole('EMPLEADO')")
public class TrabajoHoyController {

    private final ITrabajoHoyService trabajoHoyService;

    public TrabajoHoyController(ITrabajoHoyService trabajoHoyService) {
        this.trabajoHoyService = trabajoHoyService;
    }

    // GET /empleado/hoy
    @GetMapping
    public ModelAndView verHoy(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.TRABAJO_HOY);
        try {
            modelAndView.addObject("consulta", trabajoHoyService.consultar(authentication.getName()));
        } catch (ValidacionException e) {
            modelAndView.addObject("errorGeneral", e.getMessage());
        }
        return modelAndView;
    }
}
