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

// El responsable descarga el recibo de una liquidación cerrada de su unidad. El PDF lo arma el mismo
// ReciboService que usa el empleado
@Controller
@RequestMapping("/responsable/liquidaciones/{liquidacionId}/recibo")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ReciboPersonalController {

    private final ILiquidacionCerradaService liquidacionCerradaService;
    private final IReciboService reciboService;

    public ReciboPersonalController(ILiquidacionCerradaService liquidacionCerradaService,
                                    IReciboService reciboService) {
        this.liquidacionCerradaService = liquidacionCerradaService;
        this.reciboService = reciboService;
    }

    // GET /responsable/liquidaciones/{liquidacionId}/recibo
    @GetMapping
    public ResponseEntity<byte[]> descargar(@PathVariable Integer liquidacionId, Authentication authentication) {
        try {
            LiquidacionHaberesDTO liquidacion = liquidacionCerradaService
                    .obtenerDeLaUnidad(authentication.getName(), liquidacionId);
            byte[] pdf = reciboService.generarPdf(liquidacion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo-sueldo-" + liquidacion.getDni() + "-"
                                    + liquidacion.getFechaFin() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ValidacionException e) {
            // No existe o es de otra unidad: no hay recibo, vuelve a la lista, que muestra el aviso
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/responsable/liquidaciones/cerradas?reciboError"))
                    .build();
        }
    }
}