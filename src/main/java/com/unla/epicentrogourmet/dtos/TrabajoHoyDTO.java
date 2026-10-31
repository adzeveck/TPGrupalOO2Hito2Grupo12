package com.unla.epicentrogourmet.dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Respuesta a "¿trabajo hoy?": si hoy cae dentro del festival de su unidad y los datos para mostrarlo.
// Es una vista de los datos, no una copia de las entidades. No se persiste
@Getter @Setter @NoArgsConstructor
public class TrabajoHoyDTO {

    private LocalDate hoy;
    private boolean trabajaHoy;
    private String mensaje;           // "Sí, hoy trabajás...", "Todavía no...", "No: el festival terminó..."
    private PuestoEmpleadoDTO puesto; // festival, unidad y puesto del empleado (el CU anterior)
}