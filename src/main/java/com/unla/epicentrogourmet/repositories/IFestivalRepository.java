package com.unla.epicentrogourmet.repositories;


import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.Festival;

@Repository("festivalRepository")
public interface IFestivalRepository extends JpaRepository<Festival, Integer> {

	// Trae cada festival con sus unidades en una sola consulta (la colección es LAZY)
	@Query("SELECT DISTINCT f FROM Festival f LEFT JOIN FETCH f.unidades ORDER BY f.fechaInicio")
	List<Festival> findAllWithUnidades();


	// Festivales en curso o por venir: una unidad no se puede registrar en un festival terminado
	List<Festival> findByFechaFinGreaterThanEqualOrderByFechaInicioAsc(LocalDate fecha);

	// Un festival con sus unidades cargadas: la colección es LAZY y la vista la recorre fuera del service
	@Query("SELECT f FROM Festival f LEFT JOIN FETCH f.unidades WHERE f.id = :id")
	Optional<Festival> findByIdWithUnidades(@Param("id") Integer id);

}
