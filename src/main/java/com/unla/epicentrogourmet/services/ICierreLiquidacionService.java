package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.CierreLiquidacionDTO;
import com.unla.epicentrogourmet.enums.PeriodoLiquidacion;

// Cierre de liquidación por períodos del personal de la unidad del responsable logueado.
// Recibe el username (email) del responsable
public interface ICierreLiquidacionService {

    // Calcula lo que cobraría cada empleado activo por el tramo pendiente. No guarda nada
    CierreLiquidacionDTO previsualizar(String username, PeriodoLiquidacion periodo);

    // Recalcula y guarda la liquidación de cada empleado. Devuelve cuántas guardó
    int cerrar(String username, PeriodoLiquidacion periodo);
}
