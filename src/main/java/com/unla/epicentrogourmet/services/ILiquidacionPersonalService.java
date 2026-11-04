package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.dtos.LiquidacionPersonalDTO;

// Liquidación de haberes de los empleados activos de la unidad del responsable logueado.
// Recibe el username (email) del responsable
public interface ILiquidacionPersonalService {

    LiquidacionPersonalDTO calcular(String username);

    // Liquidación de un empleado puntual de la unidad del responsable (para su recibo)
    LiquidacionHaberesDTO calcularDeEmpleado(String username, Integer empleadoId);
}