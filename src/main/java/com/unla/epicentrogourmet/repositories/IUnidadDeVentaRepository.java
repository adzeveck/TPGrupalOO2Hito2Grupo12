package com.unla.epicentrogourmet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.UnidadDeVenta;

// Un solo repositorio para toda la jerarquía: devuelve FoodTruck y PuestoDesarmable
@Repository("unidadDeVentaRepository")
public interface IUnidadDeVentaRepository extends JpaRepository<UnidadDeVenta, Integer> {

	boolean existsByCodigo(String codigo);

	// La unidad del responsable logueado
	Optional<UnidadDeVenta> findByResponsableUsername(String username);

	// La unidad donde trabaja un empleado no necesita consulta: es personal.getUnidad()
}
