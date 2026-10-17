package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.RegistroUnidadDTO;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;

public interface IUnidadDeVentaService {

    // Crea la unidad y el usuario responsable, y los asigna al festival elegido
    UnidadDeVenta registrar(RegistroUnidadDTO dto);
}
