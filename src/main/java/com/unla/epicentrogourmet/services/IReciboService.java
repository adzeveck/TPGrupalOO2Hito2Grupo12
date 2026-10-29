package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;

// Recibo de sueldo en PDF: es la liquidación ya calculada, impresa
public interface IReciboService {

    byte[] generarPdf(LiquidacionHaberesDTO liquidacion);
}