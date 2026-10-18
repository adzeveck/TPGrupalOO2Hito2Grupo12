package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un renglón del formulario de pedido: un plato del menú y cuántos se piden.
// nombre y precio son solo para mostrar: el service los vuelve a leer de la base
@Getter @Setter @NoArgsConstructor
public class ItemPedidoDTO {

	@NotNull
	private Integer platoId;

	private String nombre;

	private BigDecimal precio;

	// Vacío o 0 = ese plato no se pidió
	@PositiveOrZero(message = "La cantidad no puede ser negativa")
	private Integer cantidad;

	public ItemPedidoDTO(Integer platoId, String nombre, BigDecimal precio) {
		this.platoId = platoId;
		this.nombre = nombre;
		this.precio = precio;
		this.cantidad = 0;
	}

	public boolean isPedido() {
		return cantidad != null && cantidad > 0;
	}
}
