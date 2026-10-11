package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Plato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal precio;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal costo;

	// Baja lógica: un plato que hoy no se prepara sigue referenciado en pedidos viejos
	private boolean activo = true;
}
