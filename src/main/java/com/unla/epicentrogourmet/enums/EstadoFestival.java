package com.unla.epicentrogourmet.enums;

import java.time.LocalDate;

// En qué momento está un festival respecto de una fecha. No se persiste: se calcula con las fechas del festival
public enum EstadoFestival {
	POR_VENIR("Por venir"),
	EN_CURSO("En curso"),
	TERMINADO("Terminado");

	private final String descripcion;

	EstadoFestival(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	// El primer y el último día del festival cuentan como "en curso"
	public static EstadoFestival de(LocalDate inicio, LocalDate fin, LocalDate hoy) {
		if (hoy.isBefore(inicio)) {
			return POR_VENIR;
		}
		if (hoy.isAfter(fin)) {
			return TERMINADO;
		}
		return EN_CURSO;
	}
}
