package com.unla.epicentrogourmet.entities;

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
}
