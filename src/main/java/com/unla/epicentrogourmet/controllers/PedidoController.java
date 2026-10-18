package com.unla.epicentrogourmet.controllers;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IPedidoService;

import jakarta.validation.Valid;

// Alta de pedidos por el cajero. Que sea cajero (y no cocinero) lo valida el service
@Controller
@RequestMapping("/empleado/pedidos")
@PreAuthorize("hasRole('EMPLEADO')")
public class PedidoController {

	private final IPedidoService pedidoService;

	public PedidoController(IPedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	// GET /empleado/pedidos: los pedidos de hoy de la unidad, con el aviso del que se acaba de crear
	@GetMapping
	public ModelAndView pedidosDeHoy(Authentication authentication, @RequestParam(required = false) Integer creado) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PEDIDO_LIST);
		modelAndView.addObject("hoy", LocalDate.now());
		try {
			modelAndView.addObject("unidad", pedidoService.getUnidadDelCajero(authentication.getName()));
			modelAndView.addObject("pedidos", pedidoService.getPedidosDeHoy(authentication.getName()));
			modelAndView.addObject("creado", creado);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// GET /empleado/pedidos/nuevo: el formulario con un renglón por plato activo
	@GetMapping("/nuevo")
	public ModelAndView form(Authentication authentication) {
		try {
			return formConDatos(authentication, pedidoService.prepararFormulario(authentication.getName()));
		} catch (ValidacionException e) {
			// No es cajero, no tiene unidad o no hay festival activo hoy: no hay formulario que mostrar
			return vistaError(e);
		}
	}

	// POST /empleado/pedidos: el formulario completado
	@PostMapping
	public ModelAndView registrar(Authentication authentication, @Valid @ModelAttribute("pedido") PedidoDTO pedido,
			BindingResult bindingResult) {
		try {
			// 1. Errores de formato (anotaciones del DTO): vuelve al formulario con lo que había cargado
			if (bindingResult.hasErrors()) {
				return formConDatos(authentication, pedido);
			}
			// 2. Reglas de negocio (service)
			Pedido creado = pedidoService.registrar(authentication.getName(), pedido);
			// 3. Redirect (POST-Redirect-GET): un F5 no vuelve a cargar el pedido
			return new ModelAndView("redirect:" + ViewRouteHelper.PEDIDOS_ROOT + "?creado=" + creado.getId());
		} catch (ValidacionException e) {
			// Sin campo = error general (no es cajero, no hay festival activo): no se vuelve al formulario
			if (e.getCampo() == null) {
				return vistaError(e);
			}
			// Con campo = error del pedido (ningún plato, plato no disponible): el error va en el formulario
			bindingResult.rejectValue(e.getCampo(), "error.pedido", e.getMessage());
			return formConDatos(authentication, pedido);
		}
	}

	// El formulario siempre muestra a qué unidad se carga y con qué fecha
	private ModelAndView formConDatos(Authentication authentication, PedidoDTO pedido) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PEDIDO_FORM);
		modelAndView.addObject("pedido", pedido);
		modelAndView.addObject("unidad", pedidoService.getUnidadDelCajero(authentication.getName()));
		modelAndView.addObject("hoy", LocalDate.now());
		return modelAndView;
	}

	private ModelAndView vistaError(ValidacionException e) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PEDIDO_FORM);
		modelAndView.addObject("errorGeneral", e.getMessage());
		return modelAndView;
	}
}
