package com.unla.epicentrogourmet.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.unla.epicentrogourmet.enums.CategoriaCocina;

// Cocinero de los datos de prueba: subtotal de 250.000 en un festival del 01/10 al 15/11
class SueldoAcumuladoTest {

	private Festival festival() {
		Costo costo = new Costo();
		costo.setSueldoBase(new BigDecimal("100000.00"));
		costo.setPlusCocinero(new BigDecimal("150000.00"));
		Festival festival = new Festival();
		festival.setFechaInicio(LocalDate.of(2026, 10, 1));
		festival.setFechaFin(LocalDate.of(2026, 11, 15));
		festival.setCosto(costo);
		return festival;
	}

	private Cocinero cocinero(LocalDate ingreso) {
		Cocinero cocinero = new Cocinero();
		cocinero.setFechaIngreso(ingreso);
		cocinero.setCategoria(CategoriaCocina.COCINERO);
		return cocinero;
	}

	private void assertMonto(String esperado, BigDecimal monto) {
		assertEquals(0, new BigDecimal(esperado).compareTo(monto));
	}

	@Test
	void laEscalaSeAplicaALosDiasAcumulados() {
		Cocinero cocinero = cocinero(LocalDate.of(2020, 3, 1));

		assertMonto("125000.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 10, 15)));
		assertMonto("250000.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 10, 16)));
		assertMonto("375000.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 11, 4)));
	}

	@Test
	void unPeriodoDentroDeUnBloqueYaContadoNoSumaSueldo() {
		Cocinero cocinero = cocinero(LocalDate.of(2020, 3, 1));

		BigDecimal alEmpezar = cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 10, 20));
		BigDecimal alTerminar = cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 10, 25));

		assertMonto("0.00", alTerminar.subtract(alEmpezar));
	}

	@Test
	void antesDeSuPrimerDiaNoLeCorrespondeNada() {
		Cocinero cocinero = cocinero(LocalDate.of(2026, 10, 10));

		assertMonto("0.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 10, 9)));
		assertMonto("0.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 9, 30)));
	}

	@Test
	void quienIngresaConElFestivalEmpezadoAcumulaDesdeSuIngreso() {
		Cocinero cocinero = cocinero(LocalDate.of(2026, 10, 10));

		// Del 10/10 al 04/11 son 26 días: 100%
		assertMonto("250000.00", cocinero.calcularSueldoAcumulado(festival(), LocalDate.of(2026, 11, 4)));
	}
}
