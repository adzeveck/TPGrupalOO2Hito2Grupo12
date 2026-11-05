package com.unla.epicentrogourmet.controllers;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.services.ILiquidacionCerradaService;
import com.unla.epicentrogourmet.services.IReciboService;

// El empleado descarga el recibo de una de sus liquidaciones cerradas. Se busca por id y por el usuario
// logueado, así que nadie puede bajar el recibo de otro empleado
@Controller
@RequestMapping("/empleado/sueldo/{liquidacionId}/recibo")
@PreAuthorize("hasRole('EMPLEADO')")
public class ReciboController {

    private final ILiquidacionCerradaService liquidacionCerradaService;
    private final IReciboService reciboService;

    public ReciboController(ILiquidacionCerradaService liquidacionCerradaService, IReciboService reciboService) {
        this.liquidacionCerradaService = liquidacionCerradaService;
        this.reciboService = reciboService;
    }

    // GET /empleado/sueldo/{liquidacionId}/recibo
    @GetMapping
    public ResponseEntity<byte[]> descargar(@PathVariable Integer liquidacionId, Authentication authentication) {
        try {
            LiquidacionHaberesDTO liquidacion = liquidacionCerradaService
                    .obtenerDelEmpleado(authentication.getName(), liquidacionId);
            byte[] pdf = reciboService.generarPdf(liquidacion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo-sueldo-" + liquidacion.getDni() + "-"
                                    + liquidacion.getFechaFin() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ValidacionException e) {
            // No existe o no es suya: no hay recibo, vuelve a su lista, que muestra el aviso
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/empleado/sueldo?reciboError")).build();
        }
    }
}
