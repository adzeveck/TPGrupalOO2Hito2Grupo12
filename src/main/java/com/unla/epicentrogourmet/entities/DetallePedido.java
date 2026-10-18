package com.unla.epicentrogourmet.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Solo plato y cantidad: el precio y el costo ya están en Plato
@Entity
@Getter @Setter @NoArgsConstructor
public class DetallePedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Dueño de la FK pedido_id. Se setea con Pedido.agregarDetalle(...)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plato_id", nullable = false)
	private Plato plato;

	private int cantidad;
}
