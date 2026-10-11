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

// Montos fijos de un festival: nada de lo que usan el canon y la liquidación queda hardcodeado
@Entity
@Getter @Setter @NoArgsConstructor
public class Costo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal sueldoBase;

	@Column(name = "costo_por_m2", nullable = false, precision = 12, scale = 2)
	private BigDecimal costoPorM2;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal costoPorMinutoMontaje;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal plusElectricidad;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal montoPorAnioAntiguedad;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal plusCocinero;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal plusAyudante;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal plusLavaplatos;
}
