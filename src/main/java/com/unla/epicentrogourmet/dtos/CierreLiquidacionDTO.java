package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Vista previa del cierre: lo que cobra cada empleado por el tramo pendiente. No se persiste
@Getter @Setter @NoArgsConstructor
public class CierreLiquidacionDTO {

    private String unidad;
    private String festival;
    private List<LiquidacionHaberesDTO> liquidaciones = new ArrayList<>();
    private List<String> sinDiasPorLiquidar = new ArrayList<>();   // empleados sin días pendientes
    private BigDecimal total = BigDecimal.ZERO;
}
