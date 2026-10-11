package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Sin festival ni unidad: se llega navegando Festival -> UnidadDeVenta -> Pedido
@Entity
@Getter @Setter @NoArgsConstructor
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	// Composición encabezado-detalle, como una factura
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Set<DetallePedido> detalles = new HashSet<>();
}
