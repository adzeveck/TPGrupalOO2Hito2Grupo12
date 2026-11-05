package com.unla.epicentrogourmet.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.unla.epicentrogourmet.dtos.CierreCajaDTO;
import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.entities.Plato;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;

// Operaciones de pedidos para el empleado cajero.
public interface IPedidoService {

	// true si el usuario es cajero: permite mostrar u ocultar sus acciones.
	boolean puedeCargarPedidos(String username);

	// La unidad donde trabaja el cajero.
	UnidadDeVenta getUnidadDelCajero(String username);

	// Formulario vacío, con un renglón por cada plato activo de la unidad.
	PedidoDTO prepararFormulario(String username);

	// Valida y guarda un pedido con fecha de hoy.
	Pedido registrar(String username, PedidoDTO dto);

	// Listado de pedidos de hoy de la unidad.
	List<Pedido> getPedidosDeHoy(String username);

	// Busca pedidos de la unidad por fecha y plato, con paginación.
	Page<Pedido> buscarPedidos(
			String username,
			LocalDate desde,
			LocalDate hasta,
			Integer platoId,
			Pageable pageable);

	// Platos de la unidad para cargar las opciones del filtro.
	List<Plato> getPlatosParaFiltro(String username);

	// Cierre de caja: cantidad de pedidos, total y unidades vendidas por plato.
	CierreCajaDTO getCierreDeCaja(String username);

	// Guarda el cierre de caja de hoy.
	void cerrarCaja(String username);

	// Indica si la unidad ya cerró la caja de hoy.
	boolean isCajaCerrada(String username);

	// Anula un pedido de hoy de la unidad del cajero.
	void anular(String username, Integer pedidoId);
}