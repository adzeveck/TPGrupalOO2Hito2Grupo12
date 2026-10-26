package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.PlatoFormDTO;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IPlatoService;

import jakarta.validation.Valid;

// ABM de platos de la unidad del responsable. El service valida que el plato sea de su unidad
@Controller
@RequestMapping("/responsable/platos")
@PreAuthorize("hasRole('RESPONSABLE')")
public class PlatoController {

	private final IPlatoService platoService;

	public PlatoController(IPlatoService platoService) {
		this.platoService = platoService;
	}

	// GET /responsable/platos: el menú completo, incluidos los platos dados de baja
	@GetMapping
	public ModelAndView listar(Authentication authentication,
			@RequestParam(required = false) String guardado,
			@RequestParam(required = false) String estado) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PLATO_LIST);
		try {
			modelAndView.addObject("platos", platoService.listar(authentication.getName()));
			modelAndView.addObject("guardado", guardado != null);
			// "activo" o "inactivo": el estado que quedó después de la baja/alta lógica
			modelAndView.addObject("estado", estado);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// GET /responsable/platos/nuevo
	@GetMapping("/nuevo")
	public ModelAndView formAlta() {
		return formConDatos(platoService.prepararAlta(), false);
	}

	// GET /responsable/platos/{id}/editar
	@GetMapping("/{id}/editar")
	public ModelAndView formEdicion(Authentication authentication, @PathVariable Integer id) {
		try {
			return formConDatos(platoService.prepararEdicion(authentication.getName(), id), true);
		} catch (ValidacionException e) {
			return vistaError(e);
		}
	}

	// POST /responsable/platos
	@PostMapping
	public ModelAndView crear(Authentication authentication,
			@Valid @ModelAttribute("plato") PlatoFormDTO plato, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return formConDatos(plato, false);
		}
		try {
			platoService.crear(authentication.getName(), plato);
			return new ModelAndView("redirect:" + ViewRouteHelper.PLATOS_ROOT + "?guardado");
		} catch (ValidacionException e) {
			if (e.getCampo() == null) {
				return vistaError(e);
			}
			bindingResult.rejectValue(e.getCampo(), "error.plato", e.getMessage());
			return formConDatos(plato, false);
		}
	}

	// POST /responsable/platos/{id}
	@PostMapping("/{id}")
	public ModelAndView modificar(Authentication authentication, @PathVariable Integer id,
			@Valid @ModelAttribute("plato") PlatoFormDTO plato, BindingResult bindingResult) {
		plato.setId(id); // el id del formulario es solo para volver a la misma pantalla de edición
		if (bindingResult.hasErrors()) {
			return formConDatos(plato, true);
		}
		try {
			platoService.modificar(authentication.getName(), id, plato);
			return new ModelAndView("redirect:" + ViewRouteHelper.PLATOS_ROOT + "?guardado");
		} catch (ValidacionException e) {
			if (e.getCampo() == null) {
				return vistaError(e);
			}
			bindingResult.rejectValue(e.getCampo(), "error.plato", e.getMessage());
			return formConDatos(plato, true);
		}
	}

	// POST /responsable/platos/{id}/estado: baja lógica (o reactivación) sin borrar el plato
	@PostMapping("/{id}/estado")
	public ModelAndView cambiarEstado(Authentication authentication, @PathVariable Integer id) {
		try {
			boolean activo = platoService.cambiarEstado(authentication.getName(), id);
			return new ModelAndView("redirect:" + ViewRouteHelper.PLATOS_ROOT + "?estado=" + (activo ? "activo" : "inactivo"));
		} catch (ValidacionException e) {
			return vistaError(e);
		}
	}

	private ModelAndView formConDatos(PlatoFormDTO plato, boolean edicion) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PLATO_FORM);
		modelAndView.addObject("plato", plato);
		modelAndView.addObject("edicion", edicion);
		return modelAndView;
	}

	private ModelAndView vistaError(ValidacionException e) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.PLATO_LIST);
		modelAndView.addObject("errorGeneral", e.getMessage());
		return modelAndView;
	}
}
