package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IPedidoService;

@Controller
@RequestMapping("/empleado")
@PreAuthorize("hasRole('EMPLEADO')")
public class EmpleadoController {

	private final IPedidoService pedidoService;

	public EmpleadoController(IPedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping("/index")
	public ModelAndView index(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_INDEX);
		// Las opciones de pedidos solo se muestran a los cajeros
		modelAndView.addObject("puedeCargarPedidos", pedidoService.puedeCargarPedidos(authentication.getName()));
		return modelAndView;
	}
}
