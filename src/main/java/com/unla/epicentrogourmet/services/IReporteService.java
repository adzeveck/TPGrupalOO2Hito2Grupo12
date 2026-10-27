package com.unla.epicentrogourmet.services;

import java.math.BigDecimal;
import java.util.List;

import com.unla.epicentrogourmet.dtos.PlatoDTO;
import com.unla.epicentrogourmet.dtos.RecaudacionDiariaDTO;
import com.unla.epicentrogourmet.dtos.ReporteMayoresCanonDTO;
import com.unla.epicentrogourmet.dtos.ReporteVentaDTO;

// Reportes calculados en el momento (no se persisten)
public interface IReporteService {

	// Admin: las "x" unidades de un festival con mayor canon, de mayor a menor
	List<ReporteMayoresCanonDTO> getMayoresCanon(Integer festivalId, Integer limite);

	// Responsable: el plato más pedido de su unidad
	PlatoDTO getPlatoEstrella(String username);

	// Admin: ranking de unidades de un festival por recaudación, de mayor a menor
	List<ReporteVentaDTO> getRankingRecaudacion(Integer festivalId);

	// Recaudación total de un festival: la suma de lo que vendieron todas sus unidades
	BigDecimal getRecaudacionTotal(Integer festivalId);

	// Responsable: lo que vendió su unidad en el festival
	ReporteVentaDTO getRecaudacionDeMiUnidad(String username);

	// Responsable: lo que vendió su unidad cada día del festival
	List<RecaudacionDiariaDTO> getRecaudacionDiaria(String username);
}
