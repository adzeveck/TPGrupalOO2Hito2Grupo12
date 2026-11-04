package com.unla.epicentrogourmet.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IFestivalService;
import com.unla.epicentrogourmet.services.IReporteService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final IFestivalService festivalService;
	private final IReporteService reporteService;

	public AdminController(IFestivalService festivalService, IReporteService reporteService) {
		this.festivalService = festivalService;
		this.reporteService = reporteService;
	}

	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.ADMIN_INDEX);
	}

	// Vista unificada: cada festival con su estado, sus unidades de venta y el canon de cada una
	@GetMapping("/festivales")
	public ModelAndView festivales() {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVALES);
		modelAndView.addObject("festivales", festivalService.getFestivalesConUnidades(LocalDate.now()));
		return modelAndView;
	}

	// Ranking de unidades por canon para un festival. La "x" va de 1 a las unidades de ese festival
	@GetMapping("/canon")
	public ModelAndView mayoresCanon(@RequestParam(required = false) Integer festivalId,
									 @RequestParam(required = false) Integer limite) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_CANON);
		List<Festival> festivales = festivalService.getAllWithUnidades();

		// Sin festival elegido se muestra el primero
		Integer elegido = festivalId;
		if (elegido == null && !festivales.isEmpty()) {
			elegido = festivales.get(0).getId();
		}

		// El máximo de la x es la cantidad de unidades del festival elegido
		int maximo = 0;
		for (Festival festival : festivales) {
			if (festival.getId().equals(elegido)) {
				maximo = festival.getUnidades().size();
			}
		}
		int cantidad = limite == null ? Math.min(5, maximo) : limite;

		modelAndView.addObject("festivales", festivales);
		modelAndView.addObject("festivalId", elegido);
		modelAndView.addObject("limite", cantidad);
		modelAndView.addObject("maximo", maximo);
		if (elegido != null) {
			try {
				modelAndView.addObject("reporte", reporteService.getMayoresCanon(elegido, cantidad));
			} catch (ValidacionException e) {
				modelAndView.addObject("errorGeneral", e.getMessage());
			}
		}
		return modelAndView;
	}


	// Ranking de unidades de un festival por recaudación, con el total del festival
	@GetMapping("/recaudacion")
	public ModelAndView rankingRecaudacion(@RequestParam(required = false) Integer festivalId) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_RECAUDACION);
		List<Festival> festivales = festivalService.getAllWithUnidades();

		// Igual que en el ranking por canon: sin festival elegido se muestra el primero
		Integer elegido = festivalId;
		if (elegido == null && !festivales.isEmpty()) {
			elegido = festivales.get(0).getId();
		}

		modelAndView.addObject("festivales", festivales);
		modelAndView.addObject("festivalId", elegido);
		if (elegido != null) {
			try {
				modelAndView.addObject("ranking", reporteService.getRankingRecaudacion(elegido));
				modelAndView.addObject("total", reporteService.getRecaudacionTotal(elegido));
			} catch (ValidacionException e) {
				modelAndView.addObject("errorGeneral", e.getMessage());
			}
		}
		return modelAndView;
	}
}
