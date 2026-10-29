package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// La planilla de sueldos de una unidad: la liquidación de cada empleado activo y el total.
// Es un reporte calculado en el momento, no se persiste
@Getter @Setter @NoArgsConstructor
public class LiquidacionPersonalDTO {

    private String unidad;
    private String festival;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long dias;                 // días que dura el festival

    private List<LiquidacionHaberesDTO> liquidaciones = new ArrayList<>();
    private BigDecimal totalPlanilla = BigDecimal.ZERO;   // suma de los totales a cobrar
}
