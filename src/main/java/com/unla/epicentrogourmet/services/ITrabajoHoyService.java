package com.unla.epicentrogourmet.services;

import com.unla.epicentrogourmet.dtos.TrabajoHoyDTO;

// Indica si el empleado logueado trabaja hoy. Recibe el username (email) del usuario
public interface ITrabajoHoyService {

    TrabajoHoyDTO consultar(String username);
}