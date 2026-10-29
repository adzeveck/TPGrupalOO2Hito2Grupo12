package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IPuestoEmpleadoService;

// El empleado ve su propio puesto: se busca con el username del usuario logueado,
// así que nadie puede consultar los datos de otro empleado
@Controller
@RequestMapping("/empleado/puesto")
@PreAuthorize("hasRole('EMPLEADO')")
public class PuestoEmpleadoController {

    private final IPuestoEmpleadoService puestoEmpleadoService;

    public PuestoEmpleadoController(IPuestoEmpleadoService puestoEmpleadoService) {
        this.puestoEmpleadoService = puestoEmpleadoService;
    }

    // GET /empleado/puesto
    @GetMapping
    public ModelAndView verPuesto(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PUESTO_EMPLEADO);
        try {
            modelAndView.addObject("puesto", puestoEmpleadoService.obtener(authentication.getName()));
        } catch (ValidacionException e) {
            modelAndView.addObject("errorGeneral", e.getMessage());
        }
        return modelAndView;
    }
}