package com.unla.epicentrogourmet.services;

import java.util.List;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;

// Consulta de las liquidaciones ya cerradas (guardadas en LiquidacionHaberes). De acá salen los recibos.
// Todas reciben el username (email) del usuario logueado
public interface ILiquidacionCerradaService {

    // Las del empleado logueado, la más reciente primero
    List<LiquidacionHaberesDTO> listarDelEmpleado(String username);

    // Una del empleado logueado: falla si no existe o es de otro empleado
    LiquidacionHaberesDTO obtenerDelEmpleado(String username, Integer liquidacionId);

    // Las de la unidad del responsable logueado
    List<LiquidacionHaberesDTO> listarDeLaUnidad(String username);

    // Una de la unidad del responsable: falla si no existe o es de otra unidad
    LiquidacionHaberesDTO obtenerDeLaUnidad(String username, Integer liquidacionId);
}