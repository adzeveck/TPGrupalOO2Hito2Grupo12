package com.unla.epicentrogourmet.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.dtos.ReporteVentaDTO;
import com.unla.epicentrogourmet.entities.Pedido;

@Repository("pedidoRepository")
public interface IPedidoRepository extends JpaRepository<Pedido, Integer> {

	// Los pedidos de un día de una unidad, con sus renglones y platos en la misma consulta
	// (sirve para el listado del cajero y para el cierre de caja)
	@Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.detalles d LEFT JOIN FETCH d.plato "
			+ "WHERE p.unidad.id = :unidadId AND p.fecha = :fecha ORDER BY p.id DESC")
	List<Pedido> findByUnidadIdAndFecha(@Param("unidadId") Integer unidadId, @Param("fecha") LocalDate fecha);

	// Recaudación de cada unidad de un festival, de mayor a menor. Parte de la unidad (LEFT JOIN)
	// para que una unidad sin pedidos también aparezca, con $0
	@Query("SELECT new com.unla.epicentrogourmet.dtos.ReporteVentaDTO(u.nombreComercial, u.codigo, COUNT(p), SUM(p.total)) "
			+ "FROM UnidadDeVenta u LEFT JOIN u.pedidos p "
			+ "WHERE u.festival.id = :festivalId "
			+ "GROUP BY u.id, u.nombreComercial, u.codigo "
			+ "ORDER BY SUM(p.total) DESC, u.nombreComercial ASC")
	List<ReporteVentaDTO> findRecaudacionPorUnidad(@Param("festivalId") Integer festivalId);
}
