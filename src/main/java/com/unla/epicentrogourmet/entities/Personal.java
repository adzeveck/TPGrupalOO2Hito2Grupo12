package com.unla.epicentrogourmet.entities;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

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

	// Propiedades de solo lectura para presentar el subtipo y su detalle en la vista compartida del ABM.
	@jakarta.persistence.Transient
	public String getTipoEmpleado() {
		return this instanceof Cocinero ? "COCINERO" : "CAJERO";
	}

	@jakarta.persistence.Transient
	public String getDetalleEmpleado() {
		if (this instanceof Cocinero cocinero) {
			return cocinero.getCategoria() + (cocinero.getEspecialidad() == null ? "" : " · " + cocinero.getEspecialidad());
		}
		if (this instanceof Cajero cajero) {
			return cajero.getTurno() == com.unla.epicentrogourmet.enums.Turno.MANIANA ? "Mañana" : "Noche";
		}
		return "";
	}

	// ---- Cuentas de la liquidación: las hace el empleado, que es quien tiene los datos

	// Lo que se suma al sueldo base: lo define cada tipo de empleado (plus de categoría o antigüedad)
	public abstract BigDecimal calcularAdicional(Costo costo, LocalDate hasta);

	// Años completos de antigüedad hasta una fecha
	public long calcularAniosDeAntiguedad(LocalDate hasta) {
		return Math.max(0, ChronoUnit.YEARS.between(fechaIngreso, hasta));
	}

	// Hasta 15 días: 50%. De 16 a 30: 100%. Más de 30: (int(días / 15) + 1) * 50%
	public int calcularPorcentajeLiquidacion(long dias) {
		if (dias <= 15) {
			return 50;
		}
		if (dias <= 30) {
			return 100;
		}
		return (int) ((dias / 15) + 1) * 50;
	}

	// (sueldo base + adicional) por el porcentaje que corresponde a los días del período.
	// Dinero en BigDecimal, redondeado a centavos
	public BigDecimal calcularSueldo(Costo costo, LocalDate hasta, long dias) {
		BigDecimal subtotal = costo.getSueldoBase().add(calcularAdicional(costo, hasta));
		return subtotal.multiply(BigDecimal.valueOf(calcularPorcentajeLiquidacion(dias)))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	// Primer día que falta liquidar: el siguiente al último tramo cerrado. La primera vez, el que
	// llegue último entre el inicio del festival y su fecha de ingreso
	public LocalDate calcularDesde(Festival festival, LocalDate ultimoHastaLiquidado) {
		if (ultimoHastaLiquidado != null) {
			return ultimoHastaLiquidado.plusDays(1);
		}
		return fechaIngreso.isAfter(festival.getFechaInicio()) ? fechaIngreso : festival.getFechaInicio();
	}

	// Días desde su primer día (su ingreso o el inicio del festival, el que llegue último) hasta una fecha
	public long calcularDiasAcumulados(Festival festival, LocalDate hasta) {
		return ChronoUnit.DAYS.between(calcularDesde(festival, null), hasta) + 1;
	}

	// Lo que le corresponde cobrar en total desde su primer día hasta una fecha: la escala se aplica a los
	// días acumulados y no a cada tramo. Cerrando en uno o en varios tramos, la suma de lo pagado da igual
	public BigDecimal calcularSueldoAcumulado(Festival festival, LocalDate hasta) {
		return calcularSueldo(festival.getCosto(), hasta, calcularDiasAcumulados(festival, hasta));
	}
}
