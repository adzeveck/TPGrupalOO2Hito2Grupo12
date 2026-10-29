package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.LiquidacionPersonalDTO;

// Liquidación de haberes de todo el personal activo de la unidad del responsable logueado.
// Recibe el username (email) del responsable
public interface ILiquidacionPersonalService {

    LiquidacionPersonalDTO calcular(String username);
}