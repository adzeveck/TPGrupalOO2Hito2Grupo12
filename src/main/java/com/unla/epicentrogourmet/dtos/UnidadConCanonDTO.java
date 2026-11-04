package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una fila de la vista de festivales del admin: la unidad con el canon que paga en su festival.
// El canon se calcula en el momento con los montos del festival y no se persiste
@Getter @Setter @NoArgsConstructor
public class UnidadConCanonDTO {

	private String codigo;
	private String nombreComercial;
	private String tipo;
	private BigDecimal superficie;
	private String responsable;
	private BigDecimal canon;
}
