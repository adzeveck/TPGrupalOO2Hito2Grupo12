package com.unla.epicentrogourmet.entities;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Abstracta: todo empleado es Cocinero o Cajero. La baja lógica está en usuario.enabled
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public abstract class Personal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, length = 50)
	private String nombre;

	@Column(nullable = false, length = 50)
	private String apellido;

	@Column(nullable = false, unique = true, length = 10)
	private String dni;

	@Column(nullable = false)
	private LocalDate fechaNacimiento;

	@Column(nullable = false)
	private LocalDate fechaIngreso;

	// Composición: las credenciales son de este empleado y se guardan con él
	@OneToOne(cascade = CascadeType.ALL, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User usuario;

	// Dueño de la FK unidad_id: la unidad donde trabaja. Puede ser null porque la relación es una
	// agregación (la persona existe más allá de la unidad)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "unidad_id")
	private UnidadDeVenta unidad;
}
