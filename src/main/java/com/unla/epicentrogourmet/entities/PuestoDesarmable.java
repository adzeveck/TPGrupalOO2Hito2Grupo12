package com.unla.epicentrogourmet.entities;

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
}
