package com.unla.epicentrogourmet.services;

import java.time.LocalDate;

import com.unla.epicentrogourmet.dtos.TableroResponsableDTO;

public interface ITableroService {

	// El tablero de la unidad del responsable logueado, a la fecha "hoy"
	TableroResponsableDTO getTablero(String username, LocalDate hoy);
}
