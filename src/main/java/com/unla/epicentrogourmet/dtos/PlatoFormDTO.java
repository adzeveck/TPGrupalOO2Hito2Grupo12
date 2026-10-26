package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos del formulario de alta y modificación de un plato de la unidad del responsable
@Getter @Setter @NoArgsConstructor
public class PlatoFormDTO {

	private Integer id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 100, message = "El nombre admite hasta 100 caracteres")
	private String nombre;

	// La columna es decimal(12,2): hasta 10 enteros y 2 decimales
	@NotNull(message = "El precio es obligatorio")
	@DecimalMin(value = "0.01", message = "El precio tiene que ser mayor a 0")
	@Digits(integer = 10, fraction = 2, message = "El precio admite hasta 10 enteros y 2 decimales")
	private BigDecimal precio;

	@NotNull(message = "El costo es obligatorio")
	@DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
	@Digits(integer = 10, fraction = 2, message = "El costo admite hasta 10 enteros y 2 decimales")
	private BigDecimal costo;

	// Un plato que se vende por debajo de lo que cuesta producirse es un error de carga
	@AssertTrue(message = "El precio no puede ser menor al costo del plato")
	public boolean isPrecioMayorAlCosto() {
		return precio == null || costo == null || precio.compareTo(costo) >= 0;
	}
}
