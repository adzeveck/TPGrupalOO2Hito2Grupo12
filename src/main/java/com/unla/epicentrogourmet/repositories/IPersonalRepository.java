package com.unla.epicentrogourmet.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.Personal;

// Un solo repositorio para toda la jerarquía: devuelve Cocinero y Cajero
@Repository("personalRepository")
public interface IPersonalRepository extends JpaRepository<Personal, Integer> {

	boolean existsByDni(String dni);

	Optional<Personal> findByUsuarioUsername(String username);
}
