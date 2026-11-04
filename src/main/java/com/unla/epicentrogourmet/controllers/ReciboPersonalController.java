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
import com.unla.epicentrogourmet.services.ILiquidacionPersonalService;
import com.unla.epicentrogourmet.services.IReciboService;

// El responsable descarga el recibo de un empleado de su unidad. El PDF lo arma el mismo ReciboService
// que usa el empleado para su propio recibo
@Controller
@RequestMapping("/responsable/liquidaciones/{empleadoId}/recibo")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ReciboPersonalController {

    private final ILiquidacionPersonalService liquidacionPersonalService;
    private final IReciboService reciboService;

    public ReciboPersonalController(ILiquidacionPersonalService liquidacionPersonalService,
                                    IReciboService reciboService) {
        this.liquidacionPersonalService = liquidacionPersonalService;
        this.reciboService = reciboService;
    }

    // GET /responsable/liquidaciones/{empleadoId}/recibo
    @GetMapping
    public ResponseEntity<byte[]> descargar(@PathVariable Integer empleadoId, Authentication authentication) {
        try {
            LiquidacionHaberesDTO liquidacion = liquidacionPersonalService
                    .calcularDeEmpleado(authentication.getName(), empleadoId);
            byte[] pdf = reciboService.generarPdf(liquidacion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo-sueldo-" + liquidacion.getDni() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ValidacionException e) {
            // Empleado de otra unidad, inexistente o dado de baja: no hay recibo, vuelve a la planilla
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/responsable/liquidaciones")).build();
        }
    }
}