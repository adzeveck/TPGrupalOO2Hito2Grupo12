package com.unla.epicentrogourmet.controllers;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
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

	// Los festivales con la cantidad de unidades de cada uno
	@GetMapping("/festivales")
	public ModelAndView festivales() {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVALES);
		modelAndView.addObject("festivales", festivalService.getAllWithUnidades());
		return modelAndView;
	}

	// Las unidades de un festival, cada una con su canon
	@GetMapping("/festivales/{id}/unidades")
	public ModelAndView unidadesDeFestival(@PathVariable Integer id) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_FESTIVAL_UNIDADES);
		try {
			Festival festival = festivalService.getConUnidades(id);
			List<UnidadDeVenta> unidades = festival.getUnidades().stream()
					.sorted(Comparator.comparing(UnidadDeVenta::getNombreComercial))
					.toList();
			Map<Integer, BigDecimal> canones = new HashMap<>();
			for (UnidadDeVenta unidad : unidades) {
				canones.put(unidad.getId(), unidad.calcularCanon(festival.getCosto()));
			}
			modelAndView.addObject("festival", festival);
			modelAndView.addObject("unidades", unidades);
			modelAndView.addObject("canones", canones);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Ranking de unidades por canon para un festival. La "x" la elige el admin
	@GetMapping("/canon")
	public ModelAndView mayoresCanon(@RequestParam(required = false) Integer festivalId,
			@RequestParam(required = false) Integer limite) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.ADMIN_CANON);
		List<Festival> festivales = festivalService.getAllWithUnidades();

		// Con un solo festival precargado no hace falta elegir: se muestra ese
		Integer elegido = festivalId;
		if (elegido == null && !festivales.isEmpty()) {
			elegido = festivales.get(0).getId();
		}
		int cantidad = limite == null ? 5 : limite;

		modelAndView.addObject("festivales", festivales);
		modelAndView.addObject("festivalId", elegido);
		modelAndView.addObject("limite", cantidad);
		if (elegido != null) {
			try {
				modelAndView.addObject("reporte", reporteService.getMayoresCanon(elegido, cantidad));
			} catch (ValidacionException e) {
				modelAndView.addObject("errorGeneral", e.getMessage());
			}
		}
		return modelAndView;
	}
}
