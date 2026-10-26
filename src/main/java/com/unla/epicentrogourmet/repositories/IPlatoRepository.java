package com.unla.epicentrogourmet.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.dtos.PlatoDTO;
import com.unla.epicentrogourmet.entities.Plato;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Integer> {

	// El menú vigente de una unidad. Consulta por nombre de método: Plato.unidad.id = ? AND activo = true
	List<Plato> findByUnidadIdAndActivoTrueOrderByNombre(Integer unidadId);

	// Plato estrella: primero el que aparece en más pedidos y, si empatan, el que más unidades vendió
	@Query("SELECT new com.unla.epicentrogourmet.dtos.PlatoDTO(p.nombre, SUM(d.cantidad)) "
			+ "FROM DetallePedido d JOIN d.plato p JOIN d.pedido pe "
			+ "WHERE pe.unidad.id = :unidadId "
			+ "GROUP BY p.id, p.nombre "
			+ "ORDER BY COUNT(DISTINCT pe.id) DESC, SUM(d.cantidad) DESC, p.nombre ASC")
	List<PlatoDTO> findRankingDePlatos(@Param("unidadId") Integer unidadId);
}
