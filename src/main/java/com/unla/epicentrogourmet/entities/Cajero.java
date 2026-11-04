package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.unla.epicentrogourmet.enums.Turno;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Cajero extends Personal {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Turno turno;

	@Override
	public BigDecimal calcularAdicional(Costo costo, LocalDate hasta) {
		return costo.getMontoPorAnioAntiguedad().multiply(BigDecimal.valueOf(calcularAniosDeAntiguedad(hasta)));
	}
}
