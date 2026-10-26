package com.unla.epicentrogourmet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.UnidadDeVenta;

// Un solo repositorio para toda la jerarquía: devuelve FoodTruck y PuestoDesarmable
@Repository("unidadDeVentaRepository")
public interface IUnidadDeVentaRepository extends JpaRepository<UnidadDeVenta, Integer> {

	boolean existsByCodigo(String codigo);

	// La unidad del responsable logueado, con su festival cargado para mostrar el canon sin lazy loading
	@Query("SELECT u FROM UnidadDeVenta u JOIN FETCH u.festival WHERE u.responsable.username = :username")
	Optional<UnidadDeVenta> findByResponsableUsername(@Param("username") String username);

	// La unidad donde trabaja un empleado no necesita consulta: es personal.getUnidad()
}
