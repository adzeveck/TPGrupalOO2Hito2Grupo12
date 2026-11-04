package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.unla.epicentrogourmet.enums.EstadoFestival;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tablero del responsable: lo más importante de su unidad y su festival en una sola pantalla.
// Se calcula en el momento con los servicios de cada reporte y no se persiste.
// Un indicador en null (o con su mensaje) significa que todavía no hay datos para mostrarlo
@Getter @Setter @NoArgsConstructor
public class TableroResponsableDTO {

	// La unidad
	private String unidad;
	private String codigo;
	private String tipo;

	// El festival y en qué día va
	private String festival;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private EstadoFestival estado;
	private long diasTotales;
	private long diasTranscurridos;  // contando hoy
	private long diasRestantes;
	private long diasParaEmpezar;    // solo si el festival está por venir
	private int porcentajeAvance;    // 0 a 100, para la barra de progreso

	// Recaudación a la fecha (todos los pedidos de la unidad)
	private BigDecimal recaudacion;
	private Long cantidadPedidos;

	// Pedidos de hoy
	private Long pedidosHoy;
	private BigDecimal recaudacionHoy;

	// Canon del festival
	private BigDecimal canon;

	// Personal
	private Integer empleadosActivos;
	private BigDecimal sueldosALiquidar;  // planilla de los empleados activos

	// Rentabilidad neta del inicio del festival a hoy (o al último día, si ya terminó)
	private BigDecimal rentabilidad;
	private LocalDate rentabilidadHasta;
	private String mensajeRentabilidad;

	// Plato estrella
	private String platoEstrella;
	private Long platoEstrellaCantidad;
	private String mensajePlatoEstrella;
}
