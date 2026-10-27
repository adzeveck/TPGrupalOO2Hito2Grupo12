package com.unla.epicentrogourmet.services;

import java.time.LocalDate;

import com.unla.epicentrogourmet.dtos.RentabilidadDTO;

// Rentabilidad neta de la unidad del responsable logueado
public interface IRentabilidadService {

	// Entre dos fechas del festival. Sin fechas (null), el festival completo
	RentabilidadDTO calcular(String username, LocalDate desde, LocalDate hasta);
}
