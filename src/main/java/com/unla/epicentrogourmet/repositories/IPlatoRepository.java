package com.unla.epicentrogourmet.repositories;

import java.util.List;
import java.util.Optional;

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

	// El ABM del responsable ve también los platos dados de baja
	List<Plato> findByUnidadIdOrderByNombreAsc(Integer unidadId);

	// Un plato solo se puede editar si es de la unidad del responsable logueado
	Optional<Plato> findByIdAndUnidadId(Integer id, Integer unidadId);

	boolean existsByUnidadIdAndNombreIgnoreCase(Integer unidadId, String nombre);

	// Al modificar, el plato comparado consigo mismo no cuenta como repetido
	boolean existsByUnidadIdAndNombreIgnoreCaseAndIdNot(Integer unidadId, String nombre, Integer id);

	// Plato estrella: primero el que aparece en más pedidos y, si empatan, el que más unidades vendió
	@Query("SELECT new com.unla.epicentrogourmet.dtos.PlatoDTO(p.nombre, SUM(d.cantidad)) "
			+ "FROM DetallePedido d JOIN d.plato p JOIN d.pedido pe "
			+ "WHERE pe.unidad.id = :unidadId "
			+ "GROUP BY p.id, p.nombre "
			+ "ORDER BY COUNT(DISTINCT pe.id) DESC, SUM(d.cantidad) DESC, p.nombre ASC")
	List<PlatoDTO> findRankingDePlatos(@Param("unidadId") Integer unidadId);
}
