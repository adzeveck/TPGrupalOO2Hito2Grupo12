package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;

// Liquidación de haberes del empleado logueado. Recibe el username (email) del usuario
public interface ILiquidacionService {

	// Calcula el sueldo del empleado en el festival de su unidad de venta
	LiquidacionHaberesDTO calcular(String username);
}
