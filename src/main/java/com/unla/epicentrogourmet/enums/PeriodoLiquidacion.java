package com.unla.epicentrogourmet.enums;

import java.time.LocalDate;

import com.unla.epicentrogourmet.exceptions.ValidacionException;

// Los períodos que el responsable puede elegir al cerrar una liquidación
public enum PeriodoLiquidacion {

    QUINCENA("15 días", 15),
    MES("30 días", 30),
    HASTA_HOY("Hasta hoy", 0);   // 0: no tiene largo fijo, llega hasta hoy

    private final String etiqueta;
    private final int dias;

    PeriodoLiquidacion(String etiqueta, int dias) {
        this.etiqueta = etiqueta;
        this.dias = dias;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Último día del tramo: el largo del período, pero sin pasar de hoy (solo se pagan los días ya
    // trabajados) ni del fin del festival
    public LocalDate calcularHasta(LocalDate desde, LocalDate hoy, LocalDate finFestival) {
        LocalDate hasta = dias == 0 ? hoy : desde.plusDays(dias - 1L);
        if (hasta.isAfter(hoy)) {
            hasta = hoy;
        }
        if (hasta.isAfter(finFestival)) {
            hasta = finFestival;
        }
        return hasta;
    }

    // Convierte el texto que llega del formulario: cualquier otro valor es inválido
    public static PeriodoLiquidacion desdeTexto(String texto) {
        for (PeriodoLiquidacion periodo : values()) {
            if (periodo.name().equals(texto)) {
                return periodo;
            }
        }
        throw new ValidacionException(null, "El período elegido no es válido");
    }
}
