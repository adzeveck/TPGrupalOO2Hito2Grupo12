package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Rentabilidad neta de una unidad en su festival, completo o entre dos fechas. Se calcula en el
// momento y no se persiste: (ventas - costo de los platos) - sueldos - canon
@Getter @Setter @NoArgsConstructor
public class RentabilidadDTO {

	private String unidad;
	private String festival;
	private LocalDate desde;
	private LocalDate hasta;
	private long dias;              // días del período consultado
	private long diasFestival;      // días que dura el festival

	private BigDecimal ventas;      // suma de los totales de los pedidos del período
	private BigDecimal costoPlatos; // costo de producción de los platos vendidos
	private BigDecimal gananciaBruta;
	private BigDecimal sueldos;     // proporcional a los días del período
	private BigDecimal canon;       // proporcional a los días del período
	private BigDecimal rentabilidadNeta;

	// La liquidación de cada empleado activo de la unidad (por el festival completo)
	private List<LiquidacionHaberesDTO> liquidaciones = new ArrayList<>();

	// true si el período es todo el festival: sueldos y canon van completos
	public boolean isFestivalCompleto() {
		return dias == diasFestival;
	}
}
