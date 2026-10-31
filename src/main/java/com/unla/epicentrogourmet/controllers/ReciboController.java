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
import org.springframework.web.bind.annotation.RequestMapping;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.services.ILiquidacionService;
import com.unla.epicentrogourmet.services.IReciboService;

// El empleado descarga el recibo de su propio sueldo: se calcula con el username del usuario logueado,
// así que nadie puede bajar el recibo de otro empleado
@Controller
@RequestMapping("/empleado/sueldo/recibo")
@PreAuthorize("hasRole('EMPLEADO')")
public class ReciboController {

    private final ILiquidacionService liquidacionService;
    private final IReciboService reciboService;

    public ReciboController(ILiquidacionService liquidacionService, IReciboService reciboService) {
        this.liquidacionService = liquidacionService;
        this.reciboService = reciboService;
    }

    // GET /empleado/sueldo/recibo
    @GetMapping
    public ResponseEntity<byte[]> descargar(Authentication authentication) {
        try {
            LiquidacionHaberesDTO liquidacion = liquidacionService.calcular(authentication.getName());
            byte[] pdf = reciboService.generarPdf(liquidacion);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recibo-sueldo-" + liquidacion.getDni() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (ValidacionException e) {
            // Sin empleado o sin unidad no hay recibo: vuelve a la vista del sueldo, que muestra el aviso
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/empleado/sueldo")).build();
        }
    }
}
