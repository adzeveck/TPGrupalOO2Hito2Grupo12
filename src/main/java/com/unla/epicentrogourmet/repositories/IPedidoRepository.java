package com.unla.epicentrogourmet.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.dtos.RecaudacionDiariaDTO;
import com.unla.epicentrogourmet.dtos.ReporteVentaDTO;
import com.unla.epicentrogourmet.entities.Pedido;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Integer> {

	// Pedidos de un día y una unidad, con sus detalles y platos.
	// Se usa en el listado diario del cajero y en el cierre de caja.
	@Query("SELECT DISTINCT p FROM Pedido p " +
			"LEFT JOIN FETCH p.detalles d " +
			"LEFT JOIN FETCH d.plato " +
			"WHERE p.unidad.id = :unidadId " +
			"AND p.fecha = :fecha " +
			"ORDER BY p.id DESC")
	List<Pedido> findByUnidadIdAndFecha(
			@Param("unidadId") Integer unidadId,
			@Param("fecha") LocalDate fecha);

	// IDs paginados para el buscador de pedidos.
	// La consulta no hace FETCH de la colección para que la paginación sea real.
	@Query(
			value = "SELECT p.id FROM Pedido p " +
					"WHERE p.unidad.id = :unidadId " +
					"AND (:desde IS NULL OR p.fecha >= :desde) " +
					"AND (:hasta IS NULL OR p.fecha <= :hasta) " +
					"AND (:platoId IS NULL OR EXISTS (" +
					"    SELECT d.id FROM DetallePedido d " +
					"    WHERE d.pedido = p AND d.plato.id = :platoId" +
					"))",
			countQuery = "SELECT COUNT(p) FROM Pedido p " +
					"WHERE p.unidad.id = :unidadId " +
					"AND (:desde IS NULL OR p.fecha >= :desde) " +
					"AND (:hasta IS NULL OR p.fecha <= :hasta) " +
					"AND (:platoId IS NULL OR EXISTS (" +
					"    SELECT d.id FROM DetallePedido d " +
					"    WHERE d.pedido = p AND d.plato.id = :platoId" +
					"))"
	)
	Page<Integer> buscarIdsDeUnidad(
			@Param("unidadId") Integer unidadId,
			@Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta,
			@Param("platoId") Integer platoId,
			Pageable pageable);

	// Carga los pedidos de la página junto con sus detalles y platos.
	@Query("SELECT DISTINCT p FROM Pedido p " +
			"LEFT JOIN FETCH p.detalles d " +
			"LEFT JOIN FETCH d.plato " +
			"WHERE p.id IN :ids")
	List<Pedido> buscarPedidosConDetallesPorIds(
			@Param("ids") List<Integer> ids);

	// Ventas de una unidad entre dos fechas, ambas inclusive.
	@Query("SELECT SUM(p.total) FROM Pedido p " +
			"WHERE p.unidad.id = :unidadId " +
			"AND p.fecha BETWEEN :desde AND :hasta")
	BigDecimal sumVentas(
			@Param("unidadId") Integer unidadId,
			@Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta);

	// Costo de producción de lo vendido: cantidad por costo del plato.
	@Query("SELECT SUM(d.cantidad * d.plato.costo) FROM DetallePedido d " +
			"WHERE d.pedido.unidad.id = :unidadId " +
			"AND d.pedido.fecha BETWEEN :desde AND :hasta")
	BigDecimal sumCostoPlatos(
			@Param("unidadId") Integer unidadId,
			@Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta);

	// Un pedido solo se puede anular si pertenece a la unidad del cajero.
	Optional<Pedido> findByIdAndUnidadId(Integer id, Integer unidadId);

	// Recaudación por unidad de un festival, ordenada de mayor a menor.
	@Query("SELECT new com.unla.epicentrogourmet.dtos.ReporteVentaDTO(" +
			"u.nombreComercial, u.codigo, COUNT(p), SUM(p.total)) " +
			"FROM UnidadDeVenta u LEFT JOIN u.pedidos p " +
			"WHERE u.festival.id = :festivalId " +
			"GROUP BY u.id, u.nombreComercial, u.codigo " +
			"ORDER BY SUM(p.total) DESC, u.nombreComercial ASC")
	List<ReporteVentaDTO> findRecaudacionPorUnidad(
			@Param("festivalId") Integer festivalId);

	// Recaudación de la unidad día por día.
	@Query("SELECT new com.unla.epicentrogourmet.dtos.RecaudacionDiariaDTO(" +
			"p.fecha, COUNT(p), SUM(p.total)) " +
			"FROM Pedido p " +
			"WHERE p.unidad.id = :unidadId " +
			"GROUP BY p.fecha " +
			"ORDER BY p.fecha ASC")
	List<RecaudacionDiariaDTO> findRecaudacionDiaria(
			@Param("unidadId") Integer unidadId);
}