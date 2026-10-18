package com.unla.epicentrogourmet.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.RegistroUnidadDTO;
import com.unla.epicentrogourmet.enums.TipoUnidad;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IFestivalService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

import jakarta.validation.Valid;

// Registro público (sin login) de una unidad de venta con su responsable.
// SecurityConfiguration ya deja pasar /registro/** sin sesión
@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final IUnidadDeVentaService unidadDeVentaService;
    private final IFestivalService festivalService;

    public RegistroController(IUnidadDeVentaService unidadDeVentaService, IFestivalService festivalService) {
        this.unidadDeVentaService = unidadDeVentaService;
        this.festivalService = festivalService;
    }

    // GET /registro: el formulario vacío
    @GetMapping
    public ModelAndView form() {
        return formConDatos(new RegistroUnidadDTO());
    }

    // POST /registro: el formulario completado
    @PostMapping
    public ModelAndView registrar(@Valid @ModelAttribute("registro") RegistroUnidadDTO registro,
                                  BindingResult bindingResult) {
        // 1. Errores de formato (las anotaciones del DTO): se vuelve al formulario con lo que había escrito
        if (bindingResult.hasErrors()) {
            return formConDatos(registro);
        }
        // 2. Reglas de negocio (el service): el error se muestra al lado del campo que lo causó
        try {
            unidadDeVentaService.registrar(registro);
        } catch (ValidacionException e) {
            bindingResult.rejectValue(e.getCampo(), "error.registro", e.getMessage());
            return formConDatos(registro);
        }
        // 3. Todo bien: redirect, para que un F5 no vuelva a enviar el formulario
        return new ModelAndView("redirect:" + ViewRouteHelper.LOGIN_REGISTRADO);
    }

    // El formulario siempre necesita los festivales y los tipos para los selectores
    private ModelAndView formConDatos(RegistroUnidadDTO registro) {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.REGISTRO_FORM);
        modelAndView.addObject("registro", registro);
        modelAndView.addObject("festivales", festivalService.getVigentes());
        modelAndView.addObject("tipos", TipoUnidad.values());
        return modelAndView;
    }
}
