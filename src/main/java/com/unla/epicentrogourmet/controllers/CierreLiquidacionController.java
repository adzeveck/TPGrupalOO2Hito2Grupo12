package com.unla.epicentrogourmet.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unla.epicentrogourmet.enums.PeriodoLiquidacion;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.ICierreLiquidacionService;

// El responsable cierra la liquidación del personal de su propia unidad: la unidad se busca con el
// username del usuario logueado
@Controller
@RequestMapping("/responsable/liquidaciones/cierre")
@PreAuthorize("hasRole('RESPONSABLE')")
public class CierreLiquidacionController {

    private final ICierreLiquidacionService cierreLiquidacionService;

    public CierreLiquidacionController(ICierreLiquidacionService cierreLiquidacionService) {
        this.cierreLiquidacionService = cierreLiquidacionService;
    }

    // GET /responsable/liquidaciones/cierre            -> formulario para elegir el período
    // GET /responsable/liquidaciones/cierre?periodo=X  -> vista previa de lo que cobra cada empleado
    @GetMapping
    public ModelAndView ver(@RequestParam(required = false) String periodo, Authentication authentication) {
        ModelAndView modelAndView = vista();
        if (periodo == null || periodo.isBlank()) {
            return modelAndView;
        }
        try {
            PeriodoLiquidacion elegido = PeriodoLiquidacion.desdeTexto(periodo);
            modelAndView.addObject("periodoElegido", elegido);
            modelAndView.addObject("cierre", cierreLiquidacionService.previsualizar(authentication.getName(), elegido));
        } catch (ValidacionException e) {
            modelAndView.addObject("errorGeneral", e.getMessage());
        }
        return modelAndView;
    }

    // POST /responsable/liquidaciones/cierre: confirma y guarda
    @PostMapping
    public ModelAndView confirmar(@RequestParam String periodo, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            int guardadas = cierreLiquidacionService.cerrar(authentication.getName(),
                    PeriodoLiquidacion.desdeTexto(periodo));
            // Redirect: un F5 no vuelve a enviar el formulario
            redirectAttributes.addFlashAttribute("cerradas", guardadas);
            return new ModelAndView("redirect:/responsable/liquidaciones/cierre");
        } catch (ValidacionException e) {
            ModelAndView modelAndView = vista();
            modelAndView.addObject("errorGeneral", e.getMessage());
            return modelAndView;
        }
    }

    private ModelAndView vista() {
        ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_CIERRE_LIQUIDACION);
        modelAndView.addObject("periodos", PeriodoLiquidacion.values());
        return modelAndView;
    }
}