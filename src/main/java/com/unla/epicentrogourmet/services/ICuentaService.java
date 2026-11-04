package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.CambioPasswordDTO;

// Cambio de contraseña del usuario logueado. Recibe el username (email) del usuario
public interface ICuentaService {

    void cambiarPassword(String username, CambioPasswordDTO cambio);
}