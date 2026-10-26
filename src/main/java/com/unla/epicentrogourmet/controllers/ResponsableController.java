package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.FoodTruck;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IReporteService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Controller
@RequestMapping("/responsable")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ResponsableController {

	private final IUnidadDeVentaService unidadDeVentaService;
	private final IReporteService reporteService;

	public ResponsableController(IUnidadDeVentaService unidadDeVentaService, IReporteService reporteService) {
		this.unidadDeVentaService = unidadDeVentaService;
		this.reporteService = reporteService;
	}

	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.RESPONSABLE_INDEX);
	}

	// Canon de la unidad del responsable logueado. La fórmula la resuelve la propia unidad
	@GetMapping("/canon")
	public ModelAndView canon(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_CANON);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("unidad", unidad);
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("canon", unidadDeVentaService.calcularCanon(authentication.getName()));
			// La vista muestra la fórmula que corresponde al tipo de unidad
			modelAndView.addObject("esFoodTruck", unidad instanceof FoodTruck);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Plato estrella: el más pedido de la unidad del responsable
	@GetMapping("/plato-estrella")
	public ModelAndView platoEstrella(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATO_ESTRELLA);
		try {
			modelAndView.addObject("plato", reporteService.getPlatoEstrella(authentication.getName()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}
}
