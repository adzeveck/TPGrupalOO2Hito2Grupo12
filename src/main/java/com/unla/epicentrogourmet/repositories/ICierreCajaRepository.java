package com.unla.epicentrogourmet.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.CierreCaja;

@Repository("cierreCajaRepository")
public interface ICierreCajaRepository extends JpaRepository<CierreCaja, Integer> {

	// El cierre de una unidad en un día, si ya se hizo
	Optional<CierreCaja> findByUnidadIdAndFecha(Integer unidadId, LocalDate fecha);
}
