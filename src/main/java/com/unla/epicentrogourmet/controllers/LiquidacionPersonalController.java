package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ILiquidacionPersonalService;

// El responsable liquida el personal de su propia unidad: la unidad se busca con el username
// del usuario logueado, así que nadie puede ver la planilla de otra unidad
@Controller
@RequestMapping("/responsable/liquidaciones")
@PreAuthorize("hasRole('RESPONSABLE')")
public class LiquidacionPersonalController {

    private final ILiquidacionPersonalService liquidacionPersonalService;

    public LiquidacionPersonalController(ILiquidacionPersonalService liquidacionPersonalService) {
        this.liquidacionPersonalService = liquidacionPersonalService;
    }

    // GET /responsable/liquidaciones
    @GetMapping
    public ModelAndView ver(Authentication authentication) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_LIQUIDACIONES);
        try {
            modelAndView.addObject("planilla", liquidacionPersonalService.calcular(authentication.getName()));
        } catch (ValidacionException e) {
            modelAndView.addObject("errorGeneral", e.getMessage());
        }
        return modelAndView;
    }
}
