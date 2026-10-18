package com.unla.epicentrogourmet.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.Plato;

@Repository("platoRepository")
public interface IPlatoRepository extends JpaRepository<Plato, Integer> {

	// El menú vigente de una unidad. Consulta por nombre de método: Plato.unidad.id = ? AND activo = true
	List<Plato> findByUnidadIdAndActivoTrueOrderByNombre(Integer unidadId);
}
