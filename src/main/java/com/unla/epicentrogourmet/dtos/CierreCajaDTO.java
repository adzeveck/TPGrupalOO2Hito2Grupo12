package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cierre de caja del cajero: el resumen de lo que vendió su unidad en el día.
// Se calcula en el momento con los pedidos de hoy y no se persiste
@Getter @Setter @NoArgsConstructor
public class CierreCajaDTO {

	private LocalDate fecha;
	private String unidad;
	private String cajero;          // "Diego Pereyra (turno mañana)"
	private int cantidadPedidos;
	private BigDecimal total;       // suma de los totales de los pedidos del día

	// Unidades vendidas de cada plato, de la más vendida a la menos vendida
	private Map<String, Integer> unidadesPorPlato = new LinkedHashMap<>();
}
