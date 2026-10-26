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
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IFestivalService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final IFestivalService festivalService;

	public AdminController(IFestivalService festivalService) {
		this.festivalService = festivalService;
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
}
