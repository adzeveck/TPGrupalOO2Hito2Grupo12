package com.unla.epicentrogourmet.dtos;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Dónde trabaja el empleado: su puesto, su unidad de venta y el festival de esa unidad.
// Es una vista de los datos, no una copia de las entidades: sin ids y con textos listos para mostrar
@Getter @Setter @NoArgsConstructor
public class PuestoEmpleadoDTO {
    private String empleado;
    private String dni;
    private String puesto;          // "Cocinero", "Ayudante de cocina", "Cajero (turno noche)"...
    private LocalDate fechaIngreso;

    private String unidad;          // nombre comercial
    private String codigoUnidad;
    private String tipoUnidad;      // "Food Truck" o "Puesto Desarmable"
    private BigDecimal superficie;

    private String festival;
    private String temporada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long dias;              // días que dura el festival

    private String estado;          // "Por comenzar", "En curso" o "Finalizado"
    private boolean enCurso;        // hoy cae dentro del período del festival
}
