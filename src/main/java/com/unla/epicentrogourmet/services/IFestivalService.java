package com.unla.epicentrogourmet.services;

import java.util.List;
import java.time.LocalDate;

import com.unla.epicentrogourmet.dtos.FestivalConUnidadesDTO;
import com.unla.epicentrogourmet.entities.Festival;

public interface IFestivalService {

	List<Festival> getAllWithUnidades();

	// Festivales en los que todavía se puede registrar una unidad
	List<Festival> getVigentes();

	// Un festival con sus unidades cargadas
	Festival getConUnidades(Integer id);

	// Vista unificada del admin: cada festival con su estado a la fecha "hoy" y sus unidades con el canon
	List<FestivalConUnidadesDTO> getFestivalesConUnidades(LocalDate hoy);

}

