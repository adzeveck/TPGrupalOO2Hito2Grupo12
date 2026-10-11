package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;

@Controller
@RequestMapping("/responsable")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ResponsableController {

	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.RESPONSABLE_INDEX);
	}
}
