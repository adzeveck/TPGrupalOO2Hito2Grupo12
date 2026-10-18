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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// En el diagrama no tiene unidad (se llega desde la unidad). En el código sí: es la FK, y como en el
// ejemplo de la cátedra (Degree -> Person) el hijo es el dueño de la relación y se guarda con su repositorio
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

	// Dueño de la FK unidad_id: para asignar el pedido a una unidad se hace pedido.setUnidad(unidad)
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidad_id", nullable = false)
	private UnidadDeVenta unidad;

	// Composición encabezado-detalle, como una factura. mappedBy: la FK pedido_id la escribe
	// DetallePedido.pedido. cascade: guardar el pedido guarda sus renglones
	@OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DetallePedido> detalles = new HashSet<>();

	// Mantiene los dos lados en sincronía: el renglón queda en la lista y apunta a este pedido
	public void agregarDetalle(DetallePedido detalle) {
		detalle.setPedido(this);
		detalles.add(detalle);
	}
}
