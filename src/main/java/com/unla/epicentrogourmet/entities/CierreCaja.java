package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// La caja de una unidad cerrada por un cajero. Se guarda porque, una vez cerrada, el alta y la anulación
// de pedidos de ese día quedan bloqueadas. El cierre es de la unidad: uno solo por unidad y por día.
// El detalle por plato no se guarda: sale de los pedidos del día, que ya no pueden cambiar
@Entity
@Table(name = "cierre_caja", uniqueConstraints = @UniqueConstraint(columnNames = { "unidad_id", "fecha" }))
@Getter @Setter @NoArgsConstructor
public class CierreCaja {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private LocalDate fecha;

	@Column(nullable = false)
	private Integer cantidadPedidos;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	// Como Pedido, el hijo es dueño de las FK
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidad_id", nullable = false)
	private UnidadDeVenta unidad;

	// El cajero que cerró la caja
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cajero_id", nullable = false)
	private Cajero cajero;
}
