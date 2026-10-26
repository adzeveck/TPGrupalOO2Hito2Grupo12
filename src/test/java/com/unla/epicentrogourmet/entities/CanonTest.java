package com.unla.epicentrogourmet.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

// Valores de ejemplo del script de datos: FT 12 m² con electricidad = 8.000, FT 10 m² = 5.000,
// puesto de 20 m² con 90 min de montaje = 9.100
class CanonTest {

	private Costo costo() {
		Costo costo = new Costo();
		costo.setCostoPorM2(new BigDecimal("500.00"));
		costo.setPlusElectricidad(new BigDecimal("2000.00"));
		costo.setCostoPorMinutoMontaje(new BigDecimal("10.00"));
		return costo;
	}

	@Test
	void foodTruckConElectricidadSumaElPlus() {
		FoodTruck foodTruck = new FoodTruck();
		foodTruck.setSuperficie(new BigDecimal("12.00"));
		foodTruck.setRequiereElectricidad(true);

		assertEquals(0, new BigDecimal("8000.00").compareTo(foodTruck.calcularCanon(costo())));
	}

	@Test
	void foodTruckSinElectricidadNoSumaElPlus() {
		FoodTruck foodTruck = new FoodTruck();
		foodTruck.setSuperficie(new BigDecimal("10.00"));
		foodTruck.setRequiereElectricidad(false);

		assertEquals(0, new BigDecimal("5000.00").compareTo(foodTruck.calcularCanon(costo())));
	}

	@Test
	void puestoDesarmableDescuentaElTiempoDeMontaje() {
		PuestoDesarmable puesto = new PuestoDesarmable();
		puesto.setSuperficie(new BigDecimal("20.00"));
		puesto.setTiempoMontaje(90);

		assertEquals(0, new BigDecimal("9100.00").compareTo(puesto.calcularCanon(costo())));
	}
}
