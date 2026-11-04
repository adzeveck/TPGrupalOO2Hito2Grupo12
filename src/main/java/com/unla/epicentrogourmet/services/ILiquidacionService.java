package com.unla.epicentrogourmet.services;

import java.time.LocalDate;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.Personal;

// Liquidación de haberes de un empleado en el festival de su unidad de venta
public interface ILiquidacionService {

	// La del empleado logueado. Recibe el username (email) del usuario
	LiquidacionHaberesDTO calcular(String username);

	// La de cualquier empleado: para el personal de una unidad o para restar sueldos en la rentabilidad.
	// Cubre el festival completo
	LiquidacionHaberesDTO calcular(Personal empleado);

	// La de un período dentro del festival (desde y hasta, ambos incluidos): para el cierre por períodos
	LiquidacionHaberesDTO calcular(Personal empleado, LocalDate desde, LocalDate hasta);
}
