package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Abstracta: toda unidad es FoodTruck o PuestoDesarmable. JOINED = una tabla por clase
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public abstract class UnidadDeVenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 10)
	private String codigo;

	@Column(nullable = false, length = 100)
	private String nombreComercial;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal superficie;

	// Responsable = User con ROLE_RESPONSABLE; unique porque un responsable tiene una sola unidad
	@OneToOne(optional = false)
	@JoinColumn(name = "responsable_id", nullable = false, unique = true)
	private User responsable;

	// Composición: el plato es del menú de esta unidad
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "unidad_id", nullable = false)
	private Set<Plato> platos = new HashSet<>();

	// Agregación: el empleado existe más allá de la unidad, por eso no hay REMOVE en cascada
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "unidad_id")
	private Set<Personal> staff = new HashSet<>();

	// Composición: un pedido es una venta de esta unidad
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "unidad_id", nullable = false)
	private Set<Pedido> pedidos = new HashSet<>();
}
