package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.PuestoEmpleadoDTO;

// Festival, unidad de venta y puesto del empleado logueado. Recibe el username (email) del usuario
public interface IPuestoEmpleadoService {
    PuestoEmpleadoDTO obtener(String username);
}
