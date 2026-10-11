package com.unla.epicentrogourmet.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;

@Controller
@RequestMapping("/")
public class HomeController {

	//GET Example: SERVER/index
	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.INDEX);
	}

	@GetMapping("/")
	public RedirectView redirectToHomeIndex() {
		return new RedirectView(ViewRouteHelper.ROUTE);
	}
}
