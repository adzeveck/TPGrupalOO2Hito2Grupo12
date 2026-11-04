package com.unla.epicentrogourmet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.LiquidacionHaberes;

@Repository
public interface ILiquidacionHaberesRepository extends JpaRepository<LiquidacionHaberes, Integer> {

    // Último tramo ya liquidado del empleado: el próximo período arranca al día siguiente
    Optional<LiquidacionHaberes> findFirstByEmpleadoIdOrderByHastaDesc(Integer empleadoId);
}
