package com.unla.epicentrogourmet.services;

import java.time.LocalDate;
import java.math.BigDecimal;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.Personal;

// Liquidación de haberes de un empleado en el festival de su unidad de venta
public interface ILiquidacionService {

	// La del empleado logueado. Recibe el username (email) del usuario
	LiquidacionHaberesDTO calcular(String username);

	// La de cualquier empleado: para el personal de una unidad o para restar sueldos en la rentabilidad.
	// Cubre el festival completo
	LiquidacionHaberesDTO calcular(Personal empleado);

	// Lo que se paga al cerrar un tramo (desde y hasta, ambos incluidos): la escala se aplica a los días
	// acumulados del empleado y se resta lo que ya se le pagó
	LiquidacionHaberesDTO calcularCierre(Personal empleado, LocalDate desde, LocalDate hasta, BigDecimal yaPagado);
}
