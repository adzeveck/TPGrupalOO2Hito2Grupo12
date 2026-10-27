package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lo que vendió una unidad en un día del festival. Se calcula en el momento y no se persiste
@Getter @Setter @NoArgsConstructor
public class RecaudacionDiariaDTO {

	private LocalDate fecha;
	private long cantidadPedidos;
	private BigDecimal recaudacion;

	// Lo usa la consulta que agrupa los pedidos de la unidad por fecha
	public RecaudacionDiariaDTO(LocalDate fecha, Long cantidadPedidos, BigDecimal recaudacion) {
		this.fecha = fecha;
		this.cantidadPedidos = cantidadPedidos;
		this.recaudacion = recaudacion;
	}
}
