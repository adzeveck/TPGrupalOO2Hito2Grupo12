package com.unla.epicentrogourmet.services;

import java.util.List;

import com.unla.epicentrogourmet.dtos.CierreCajaDTO;
import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;

// Alta de pedidos por el cajero. Todos los métodos reciben el username (email) del usuario logueado
public interface IPedidoService {

	// true si el usuario es un cajero: para mostrar u ocultar la opción "Nuevo pedido"
	boolean puedeCargarPedidos(String username);

	// La unidad donde trabaja el cajero
	UnidadDeVenta getUnidadDelCajero(String username);

	// El formulario vacío: un renglón por cada plato activo de la unidad
	PedidoDTO prepararFormulario(String username);

	// Valida y guarda el pedido con fecha de hoy. Devuelve el pedido guardado
	Pedido registrar(String username, PedidoDTO dto);

	// Los pedidos de hoy de la unidad del cajero
	List<Pedido> getPedidosDeHoy(String username);

	// Cierre de caja: cantidad de pedidos, total y unidades por plato de hoy
	CierreCajaDTO getCierreDeCaja(String username);

	// Borra un pedido de hoy de la unidad del cajero, para corregir un error de carga
	void anular(String username, Integer pedidoId);
}
