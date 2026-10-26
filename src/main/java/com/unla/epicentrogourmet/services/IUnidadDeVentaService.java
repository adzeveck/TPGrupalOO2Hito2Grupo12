package com.unla.epicentrogourmet.services;

import java.math.BigDecimal;

import com.unla.epicentrogourmet.dtos.RegistroUnidadDTO;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;

public interface IUnidadDeVentaService {

    // Crea la unidad y el usuario responsable, y los asigna al festival elegido
    UnidadDeVenta registrar(RegistroUnidadDTO dto);

    // La unidad del responsable logueado (username = email)
    UnidadDeVenta getDelResponsable(String username);

    // Canon de esa unidad, calculado por la propia unidad con los costos del festival
    BigDecimal calcularCanon(String username);
}
