package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lo que vendió una unidad en un festival. Se calcula en el momento y no se persiste.
// Lo usan el ranking del admin (una fila por unidad) y el reporte del responsable (su unidad)
@Getter @Setter @NoArgsConstructor
public class ReporteVentaDTO {

	private String nombreComercial;
	private String codigo;
	private long cantidadPedidos;
	private BigDecimal recaudacion;

	// Lo usa la consulta que agrupa los pedidos por unidad.
	// Una unidad sin pedidos llega con SUM = null: se muestra como $0
	public ReporteVentaDTO(String nombreComercial, String codigo, Long cantidadPedidos, BigDecimal recaudacion) {
		this.nombreComercial = nombreComercial;
		this.codigo = codigo;
		this.cantidadPedidos = cantidadPedidos;
		this.recaudacion = recaudacion == null ? BigDecimal.ZERO : recaudacion;
	}
}
