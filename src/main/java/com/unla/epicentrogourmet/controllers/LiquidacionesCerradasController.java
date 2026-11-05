package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ILiquidacionCerradaService;

// El responsable ve las liquidaciones ya cerradas de su unidad y baja el recibo de cada una
@Controller
@RequestMapping("/responsable/liquidaciones/cerradas")
@PreAuthorize("hasRole('RESPONSABLE')")
public class LiquidacionesCerradasController {

    private final ILiquidacionCerradaService liquidacionCerradaService;

    public LiquidacionesCerradasController(ILiquidacionCerradaService liquidacionCerradaService) {
        this.liquidacionCerradaService = liquidacionCerradaService;
    }

    // GET /responsable/liquidaciones/cerradas
    @GetMapping
    public ModelAndView ver(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_LIQUIDACIONES_CERRADAS);
        try {
            modelAndView.addObject("liquidaciones", liquidacionCerradaService.listarDeLaUnidad(authentication.getName()));
        } catch (ValidacionException e) {
            modelAndView.addObject("errorGeneral", e.getMessage());
        }
        return modelAndView;
    }
}