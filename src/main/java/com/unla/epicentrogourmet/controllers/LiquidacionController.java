package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ILiquidacionService;

// El empleado ve su propia liquidación: el sueldo se calcula con el username del usuario logueado,
// así que nadie puede pedir el de otro empleado
@Controller
@RequestMapping("/empleado/sueldo")
@PreAuthorize("hasRole('EMPLEADO')")
public class LiquidacionController {

	private final ILiquidacionService liquidacionService;

	public LiquidacionController(ILiquidacionService liquidacionService) {
		this.liquidacionService = liquidacionService;
	}

	// GET /empleado/sueldo
	@GetMapping
	public ModelAndView verSueldo(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.SUELDO);
		try {
			modelAndView.addObject("liquidacion", liquidacionService.calcular(authentication.getName()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}
}
