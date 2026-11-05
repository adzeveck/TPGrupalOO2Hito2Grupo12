package com.unla.epicentrogourmet.repositories;


import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unla.epicentrogourmet.entities.LiquidacionHaberes;

@Repository
public interface ILiquidacionHaberesRepository extends JpaRepository<LiquidacionHaberes, Integer> {

    // Último tramo ya liquidado del empleado: el próximo período arranca al día siguiente
    Optional<LiquidacionHaberes> findFirstByEmpleadoIdOrderByHastaDesc(Integer empleadoId);

    // Las liquidaciones cerradas de un empleado (por su usuario), la más reciente primero
    List<LiquidacionHaberes> findByEmpleadoUsuarioUsernameOrderByHastaDesc(String username);

    // Una liquidación cerrada, pero solo si es de ese empleado
    Optional<LiquidacionHaberes> findByIdAndEmpleadoUsuarioUsername(Integer id, String username);

    // Las liquidaciones cerradas de una unidad: el tramo más reciente primero, y por apellido
    List<LiquidacionHaberes> findByUnidadIdOrderByHastaDescEmpleadoApellidoAsc(Integer unidadId);

    // Una liquidación cerrada, pero solo si es de esa unidad
    Optional<LiquidacionHaberes> findByIdAndUnidadId(Integer id, Integer unidadId);

    // Suma de lo ya liquidado (pagado) al empleado. Es null si todavía no tiene ninguna
    @Query("SELECT SUM(l.total) FROM LiquidacionHaberes l WHERE l.empleado.id = :empleadoId")
    BigDecimal sumarTotalPorEmpleado(@Param("empleadoId") Integer empleadoId);
}
