package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.CambioPasswordDTO;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ICuentaService;

import jakarta.validation.Valid;

// El empleado cambia su propia contraseña: se usa el username de la sesión, no uno que venga del formulario
@Controller
@RequestMapping("/empleado/password")
@PreAuthorize("hasRole('EMPLEADO')")
public class CuentaController {

    private final ICuentaService cuentaService;

    public CuentaController(ICuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    // GET /empleado/password
    @GetMapping
    public ModelAndView form(@RequestParam(required = false) String cambiada) {
        ModelAndView modelAndView = formConDatos(new CambioPasswordDTO());
        modelAndView.addObject("cambiada", cambiada);
        return modelAndView;
    }

    // POST /empleado/password
    @PostMapping
    public ModelAndView cambiar(@Valid @ModelAttribute("cambio") CambioPasswordDTO cambio,
                                BindingResult bindingResult, Authentication authentication) {
        // 1. Errores de formato (las anotaciones del DTO)
        if (bindingResult.hasErrors()) {
            return formConDatos(cambio);
        }
        // 2. Reglas de negocio (el service): el error se muestra al lado del campo que lo causó
        try {
            cuentaService.cambiarPassword(authentication.getName(), cambio);
        } catch (ValidacionException e) {
            if (e.getCampo() != null) {
                bindingResult.rejectValue(e.getCampo(), "error.cambio", e.getMessage());
            } else {
                bindingResult.reject("error.cambio", e.getMessage());
            }
            return formConDatos(cambio);
        }
        // 3. Todo bien: redirect, para que un F5 no vuelva a enviar el formulario
        return new ModelAndView("redirect:/empleado/password?cambiada");
    }

    private ModelAndView formConDatos(CambioPasswordDTO cambio) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_PASSWORD);
        modelAndView.addObject("cambio", cambio);
        return modelAndView;
    }
}