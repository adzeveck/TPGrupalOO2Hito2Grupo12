package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ILiquidacionCerradaService;

// El empleado ve solo sus liquidaciones cerradas: se buscan con el username del usuario logueado,
// así que nadie puede ver las de otro empleado
@Controller
@RequestMapping("/empleado/sueldo")
@PreAuthorize("hasRole('EMPLEADO')")
public class LiquidacionController {

	private final ILiquidacionCerradaService liquidacionCerradaService;

	public LiquidacionController(ILiquidacionCerradaService liquidacionCerradaService) {
		this.liquidacionCerradaService = liquidacionCerradaService;
	}

	// GET /empleado/sueldo
	@GetMapping
	public ModelAndView verSueldo(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.SUELDO);
		modelAndView.addObject("liquidaciones", liquidacionCerradaService.listarDelEmpleado(authentication.getName()));
		return modelAndView;
	}
}