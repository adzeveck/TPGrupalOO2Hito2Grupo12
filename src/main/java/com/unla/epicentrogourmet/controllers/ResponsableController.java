package com.unla.epicentrogourmet.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.FoodTruck;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IRentabilidadService;
import com.unla.epicentrogourmet.services.IReporteService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Controller
@RequestMapping("/responsable")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ResponsableController {

	private final IUnidadDeVentaService unidadDeVentaService;
	private final IReporteService reporteService;
	private final IRentabilidadService rentabilidadService;

	public ResponsableController(IUnidadDeVentaService unidadDeVentaService,
								 IReporteService reporteService,
								 IRentabilidadService rentabilidadService) {
		this.unidadDeVentaService = unidadDeVentaService;
		this.reporteService = reporteService;
		this.rentabilidadService = rentabilidadService;
	}

	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.RESPONSABLE_INDEX);
	}

	// Canon de la unidad del responsable logueado. La fórmula la resuelve la propia unidad.
	@GetMapping("/canon")
	public ModelAndView canon(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_CANON);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("unidad", unidad);
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("canon", unidadDeVentaService.calcularCanon(authentication.getName()));
			modelAndView.addObject("esFoodTruck", unidad instanceof FoodTruck);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Plato estrella: el más pedido de la unidad del responsable.
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

	// Recaudación de la unidad del responsable en su festival, y total del festival como referencia.
	@GetMapping("/recaudacion")
	public ModelAndView recaudacion(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_RECAUDACION);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("reporte",
					reporteService.getRecaudacionDeMiUnidad(authentication.getName()));
			modelAndView.addObject("totalFestival",
					reporteService.getRecaudacionTotal(unidad.getFestival().getId()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Rentabilidad neta: festival completo o período seleccionado.
	@GetMapping("/rentabilidad")
	public ModelAndView rentabilidad(Authentication authentication,
									 @RequestParam(required = false)
									 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
									 @RequestParam(required = false)
									 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_RENTABILIDAD);
		try {
			modelAndView.addObject("festival",
					unidadDeVentaService.getDelResponsable(authentication.getName()).getFestival());
			modelAndView.addObject("desde", desde);
			modelAndView.addObject("hasta", hasta);
			modelAndView.addObject("rentabilidad",
					rentabilidadService.calcular(authentication.getName(), desde, hasta));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Recaudación de la unidad del responsable día por día.
	@GetMapping("/recaudacion/diaria")
	public ModelAndView recaudacionDiaria(Authentication authentication) {
		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.RESPONSABLE_RECAUDACION_DIARIA);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("unidad", unidad);
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("dias",
					reporteService.getRecaudacionDiaria(authentication.getName()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}
}
