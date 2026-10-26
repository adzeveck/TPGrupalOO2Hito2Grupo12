package com.unla.epicentrogourmet.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Reporte del plato estrella: algo visual, no una copia de Plato. Sin id, precio ni costo.
// El constructor lo usa la consulta que agrupa los renglones de pedidos por plato
@Getter @Setter @NoArgsConstructor
public class PlatoDTO {

	private String nombre;
	private Long cantidad;   // unidades vendidas
	private String festival;
	private String unidad;

	public PlatoDTO(String nombre, Long cantidad) {
		this.nombre = nombre;
		this.cantidad = cantidad;
	}
}
