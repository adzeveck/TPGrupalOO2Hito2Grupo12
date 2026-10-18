package com.unla.epicentrogourmet.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Festival {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, length = 50)
	private String temporada;

	@Column(nullable = false)
	private LocalDate fechaInicio;

	@Column(nullable = false)
	private LocalDate fechaFin;

	// Composición 1 a 1: el costo se guarda y se borra junto con el festival
	@OneToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(name = "costo_id", nullable = false, unique = true)
	private Costo costo;

	// Agregación: la unidad tiene vida propia, por eso no hay cascada.
	// mappedBy: la FK festival_id la escribe UnidadDeVenta.festival
	@OneToMany(mappedBy = "festival", fetch = FetchType.LAZY)
	private Set<UnidadDeVenta> unidades = new HashSet<>();
}
