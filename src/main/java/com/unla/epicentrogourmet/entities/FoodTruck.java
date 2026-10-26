package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class FoodTruck extends UnidadDeVenta {

	@Column(nullable = false, length = 10)
	private String patente;

	private boolean requiereElectricidad;

	@Override
	public String getTipo() {
		return "Food Truck";
	}


	@Override
	public String getPrefijoCodigo() {
		return "FT";
	}

	// Superficie por el costo del m² + el plus si necesita electricidad
	@Override
	public BigDecimal calcularCanon(Costo costo) {
		BigDecimal canon = getSuperficie().multiply(costo.getCostoPorM2());
		if (requiereElectricidad) {
			canon = canon.add(costo.getPlusElectricidad());
		}
		return canon;
	}

}