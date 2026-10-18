package com.unla.epicentrogourmet.dtos;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos del formulario de alta de pedido: un renglón por cada plato activo de la unidad.
// No lleva fecha ni total: la fecha es la de hoy y el total lo calcula el service
@Getter @Setter @NoArgsConstructor
public class PedidoDTO {

	// @Valid: valida también cada renglón (la cantidad no negativa)
	@Valid
	private List<ItemPedidoDTO> items = new ArrayList<>();

	// Validación de carga: un pedido sin ningún plato no es un pedido
	@AssertTrue(message = "Cargá al menos un plato con cantidad mayor a 0")
	public boolean isAlgunPlatoPedido() {
		return items.stream().anyMatch(ItemPedidoDTO::isPedido);
	}
}
