package com.unla.epicentrogourmet.services;

import java.util.List;

import com.unla.epicentrogourmet.dtos.PlatoFormDTO;
import com.unla.epicentrogourmet.entities.Plato;

// ABM de platos del responsable. Todos los métodos reciben el username (email) del usuario logueado
public interface IPlatoService {

	// Los platos de su unidad, incluidos los dados de baja
	List<Plato> listar(String username);

	// El formulario vacío para un plato nuevo
	PlatoFormDTO prepararAlta();

	// El formulario con los datos del plato a modificar
	PlatoFormDTO prepararEdicion(String username, Integer platoId);

	void crear(String username, PlatoFormDTO dto);

	void modificar(String username, Integer platoId, PlatoFormDTO dto);

	// Baja lógica o reactivación: el plato nunca se borra, porque los pedidos viejos lo referencian.
	// Devuelve el estado nuevo (true = activo)
	boolean cambiarEstado(String username, Integer platoId);
}
