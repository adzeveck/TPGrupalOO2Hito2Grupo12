package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Reporte de la liquidación de un empleado en un período. Puede venir calculada en el momento o de una
// liquidación ya cerrada (guardada). Es una vista de los datos, no una copia de la entidad: textos listos
// para mostrar y ids solo para armar los enlaces
@Getter @Setter @NoArgsConstructor
public class LiquidacionHaberesDTO {

	private Integer liquidacionId;    // solo si viene de una liquidación cerrada
	private LocalDate fechaCierre;    // idem: el día en que se cerró
	private Integer empleadoId;
	private String empleado;
	private String dni;
	private String puesto;          // "Cocinero", "Ayudante de cocina", "Cajero (turno noche)"...
	private String unidad;
	private String festival;
	private LocalDate fechaInicio;  // primer día liquidado (en la liquidación general, el inicio del festival)
	private LocalDate fechaFin;     // último día liquidado (en la liquidación general, el fin del festival)
	private long dias;              // días del período liquidado
	private long diasAcumulados;      // desde su primer día hasta el fin del tramo: con estos días se aplica la escala
	private BigDecimal corresponde;   // subtotal * porcentaje: lo que le toca cobrar en total
	private BigDecimal yaPagado;      // lo liquidado en cierres anteriores

	private BigDecimal sueldoBase;
	private String conceptoAdicional; // "Plus por categoría" (cocina) o "Antigüedad (3 años)" (cajero)
	private BigDecimal adicional;
	private BigDecimal subtotal;      // sueldo base + adicional

	private int porcentaje;           // 50, 100, 150...: sale de los días acumulados
	private BigDecimal total;         // lo que se paga en este cierre: corresponde menos yaPagado
}
