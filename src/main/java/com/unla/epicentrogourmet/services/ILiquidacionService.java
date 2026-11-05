package com.unla.epicentrogourmet.services;

import java.time.LocalDate;
import java.math.BigDecimal;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.Personal;

// Liquidación de haberes de un empleado en el festival de su unidad de venta
public interface ILiquidacionService {

	// La del empleado logueado. Recibe el username (email) del usuario
	LiquidacionHaberesDTO calcular(String username);

	// La de cualquier empleado: para la planilla del personal de una unidad.
	// Cubre el festival completo
	LiquidacionHaberesDTO calcular(Personal empleado);

	// Lo que se paga al cerrar un tramo (desde y hasta, ambos incluidos): la escala se aplica a los días
	// acumulados del empleado y se resta lo que ya se le pagó. La rentabilidad usa la misma cuenta
	LiquidacionHaberesDTO calcularCierre(Personal empleado, LocalDate desde, LocalDate hasta, BigDecimal yaPagado);
}
