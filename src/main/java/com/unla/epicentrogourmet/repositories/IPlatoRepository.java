package com.unla.epicentrogourmet.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.Plato;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Integer> {

	// El menú vigente de una unidad: el plato no conoce su unidad, se llega desde la colección de la unidad
	@Query("SELECT p FROM UnidadDeVenta u JOIN u.platos p WHERE u.id = :unidadId AND p.activo = true ORDER BY p.nombre")
	List<Plato> findActivosByUnidadId(@Param("unidadId") Integer unidadId);
}
