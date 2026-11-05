package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cierre de caja del cajero: el resumen de lo que vendió su unidad en el día, para mostrar en la vista.
// Se arma con los pedidos de hoy. Lo que se guarda al cerrar la caja es la entidad CierreCaja
@Getter @Setter @NoArgsConstructor
public class CierreCajaDTO {

	private LocalDate fecha;
	private String unidad;
	private String cajero;          // "Diego Pereyra (turno mañana)"
	private int cantidadPedidos;
	private BigDecimal total;       // suma de los totales de los pedidos del día
	private boolean cerrada;        // ya se cerró la caja de hoy
	private String cerradoPor;      // el cajero que la cerró, si está cerrada
	private boolean puedeCerrar;    // sigue abierta y hoy hay festival activo

	// Unidades vendidas de cada plato, de la más vendida a la menos vendida
	private Map<String, Integer> unidadesPorPlato = new LinkedHashMap<>();
}
