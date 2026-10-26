package com.unla.epicentrogourmet.services;

import java.util.List;

import com.unla.epicentrogourmet.dtos.PlatoDTO;
import com.unla.epicentrogourmet.dtos.ReporteMayoresCanonDTO;

// Reportes calculados en el momento (no se persisten)
public interface IReporteService {

	// Admin: las "x" unidades de un festival con mayor canon, de mayor a menor
	List<ReporteMayoresCanonDTO> getMayoresCanon(Integer festivalId, Integer limite);

	// Responsable: el plato más pedido de su unidad
	PlatoDTO getPlatoEstrella(String username);
}
