package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.unla.epicentrogourmet.enums.EstadoFestival;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Vista unificada del admin: un festival con su estado, sus unidades de venta y el canon de cada una
@Getter @Setter @NoArgsConstructor
public class FestivalConUnidadesDTO {

	private Integer id;
	private String nombre;
	private String temporada;
	private LocalDate fechaInicio;
	private LocalDate fechaFin;
	private EstadoFestival estado;
	private List<UnidadConCanonDTO> unidades = new ArrayList<>();
	private BigDecimal totalCanon = BigDecimal.ZERO;
}
