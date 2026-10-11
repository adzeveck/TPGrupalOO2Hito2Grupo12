package com.unla.epicentrogourmet.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IFestivalService;

@Controller
@RequestMapping("/")
public class HomeController {

	private final IFestivalService festivalService;

	public HomeController(IFestivalService festivalService) {
		this.festivalService = festivalService;
	}

	// Página pública: info de los festivales precargados
	@GetMapping("/index")
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.INDEX);
		modelAndView.addObject("festivales", festivalService.getAllWithUnidades());
		return modelAndView;
	}

	@GetMapping("/")
	public RedirectView redirectToHomeIndex() {
		return new RedirectView(ViewRouteHelper.ROUTE);
	}
}
