package com.unla.epicentrogourmet.entities;

import com.unla.epicentrogourmet.enums.CategoriaCocina;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cocinero extends Personal {

	// El monto del plus no se guarda acá: se busca en Costo según la categoría
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoriaCocina categoria;

	// La "tarea especial" del enunciado
	@Column(length = 100)
	private String especialidad;
}
