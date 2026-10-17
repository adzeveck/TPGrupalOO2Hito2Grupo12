package com.unla.epicentrogourmet.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unla.epicentrogourmet.helpers.ViewRouteHelper;


@Controller
public class UserController {

	@GetMapping("/login")
	public String login(Model model,
						@RequestParam(required=false) String error,
						@RequestParam(required=false) String disabled,
						@RequestParam(required=false) String logout,
						@RequestParam(required=false) String registrado){
		model.addAttribute("error", error);
		model.addAttribute("disabled", disabled);
		model.addAttribute("logout", logout);
		model.addAttribute("registrado", registrado);

		return ViewRouteHelper.USER_LOGIN;
	}

	// Después del login, cada rol va a su página de inicio
	@GetMapping("/loginsuccess")
	public String loginCheck(Authentication authentication) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			switch (authority.getAuthority()) {
				case "ROLE_ADMIN": return "redirect:" + ViewRouteHelper.ADMIN_ROOT;
				case "ROLE_RESPONSABLE": return "redirect:" + ViewRouteHelper.RESPONSABLE_ROOT;
				case "ROLE_EMPLEADO": return "redirect:" + ViewRouteHelper.EMPLEADO_ROOT;
				default: break;
			}
		}
		return "redirect:" + ViewRouteHelper.ROUTE;
	}
}
