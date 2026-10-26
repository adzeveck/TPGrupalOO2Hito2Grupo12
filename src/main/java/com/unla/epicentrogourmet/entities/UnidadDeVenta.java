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
import jakarta.persistence.ManyToOne;
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

	// Dueño de la FK festival_id. En el diagrama no se dibuja (se llega desde Festival.unidades),
	// pero en el código el hijo apunta a su padre, como Degree -> Person en el ejemplo de la cátedra
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "festival_id", nullable = false)
	private Festival festival;

	// Composición: el plato es del menú de esta unidad. mappedBy: la FK la escribe Plato.unidad
	@OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Plato> platos = new HashSet<>();

	// Agregación: el empleado existe más allá de la unidad, por eso no hay cascada.
	// mappedBy: la FK la escribe Personal.unidad
	@OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY)
	private Set<Personal> staff = new HashSet<>();

	// Composición: un pedido es una venta de esta unidad. mappedBy: la FK unidad_id la escribe
	// Pedido.unidad, así que un pedido nuevo se guarda con pedido.setUnidad(...) + su repositorio
	@OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Pedido> pedidos = new HashSet<>();

	// Polimorfismo: cada subclase informa su tipo. No es una columna (JPA mapea los atributos, no los métodos)
	public abstract String getTipo();

	// Lógica propia del código (enunciado): prefijo de la subclase + 8 dígitos = 10 caracteres
	public abstract String getPrefijoCodigo();

	// Canon del festival: cada subclase aplica su fórmula con los montos del festival
	public abstract BigDecimal calcularCanon(Costo costo);

	public boolean tieneCodigoValido() {
		return codigo != null && codigo.matches(getPrefijoCodigo() + "\\d{8}");
	}

}
