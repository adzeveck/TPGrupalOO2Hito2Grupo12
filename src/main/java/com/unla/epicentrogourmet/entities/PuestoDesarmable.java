package com.unla.epicentrogourmet.entities;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class PuestoDesarmable extends UnidadDeVenta {

	private int cantCarpas;

	// En minutos
	private int tiempoMontaje;

	@Override
	public String getTipo() {
		return "Puesto Desarmable";
	}


	@Override
	public String getPrefijoCodigo() {
		return "PD";
	}

	// Superficie por el costo del m² menos lo que cuesta el tiempo de montaje
	@Override
	public BigDecimal calcularCanon(Costo costo) {
		BigDecimal porSuperficie = getSuperficie().multiply(costo.getCostoPorM2());
		BigDecimal descuentoMontaje = costo.getCostoPorMinutoMontaje()
				.multiply(BigDecimal.valueOf(tiempoMontaje));
		return porSuperficie.subtract(descuentoMontaje);
	}

}
