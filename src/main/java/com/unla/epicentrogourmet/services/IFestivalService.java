package com.unla.epicentrogourmet.services;

import java.util.List;
import java.time.LocalDate;

import com.unla.epicentrogourmet.entities.Festival;

public interface IFestivalService {

	List<Festival> getAllWithUnidades();

	// Festivales en los que todavía se puede registrar una unidad
	List<Festival> getVigentes();

	// Un festival con sus unidades cargadas
	Festival getConUnidades(Integer id);

}

