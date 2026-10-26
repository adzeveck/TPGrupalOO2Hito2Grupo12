package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Reporte del admin: las unidades de un festival ordenadas por canon. Se calcula en el momento
// y no se persiste. Sin datos del responsable ni de la unidad: solo lo que pide el enunciado
@Getter @Setter @NoArgsConstructor
public class ReporteMayoresCanonDTO {

	private String nombreComercial;
	private String codigo;
	private String tipo;
	private BigDecimal canon;
}
