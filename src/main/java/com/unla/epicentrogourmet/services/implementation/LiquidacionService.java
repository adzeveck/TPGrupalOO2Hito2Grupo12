package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.Cajero;
import com.unla.epicentrogourmet.entities.Cocinero;
import com.unla.epicentrogourmet.entities.Costo;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.services.ILiquidacionService;

@Service("liquidacionService")
public class LiquidacionService implements ILiquidacionService {

	private final IPersonalRepository personalRepository;

	public LiquidacionService(IPersonalRepository personalRepository) {
		this.personalRepository = personalRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public LiquidacionHaberesDTO calcular(String username) {
		Personal empleado = personalRepository.findByUsuarioUsername(username)
				.orElseThrow(() -> new ValidacionException(null, "Tu usuario no corresponde a un empleado"));
		return calcular(empleado);
	}

	// La misma cuenta para cualquier empleado, no solo el logueado: la usan la liquidación del
	// personal de una unidad y la rentabilidad neta (que resta los sueldos)
	@Override
	@Transactional(readOnly = true)
	public LiquidacionHaberesDTO calcular(Personal empleado) {
		UnidadDeVenta unidad = empleado.getUnidad();
		if (unidad == null) {
			throw new ValidacionException(null, "El empleado no está asignado a ninguna unidad de venta");
		}
		Festival festival = unidad.getFestival();
		Costo costo = festival.getCosto(); // todos los montos salen de los costos del festival: nada hardcodeado

		LiquidacionHaberesDTO dto = new LiquidacionHaberesDTO();
		dto.setEmpleado(empleado.getNombre() + " " + empleado.getApellido());
		dto.setDni(empleado.getDni());
		dto.setUnidad(unidad.getNombreComercial());
		dto.setFestival(festival.getNombre());
		dto.setFechaInicio(festival.getFechaInicio());
		dto.setFechaFin(festival.getFechaFin());
		dto.setDias(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
		dto.setSueldoBase(costo.getSueldoBase());

		// El tipo de empleado se sabe por la subclase (instanceof), no por un atributo
		if (empleado instanceof Cocinero cocinero) {
			switch (cocinero.getCategoria()) {
				case COCINERO -> {
					dto.setPuesto("Cocinero");
					dto.setAdicional(costo.getPlusCocinero());
				}
				case AYUDANTE -> {
					dto.setPuesto("Ayudante de cocina");
					dto.setAdicional(costo.getPlusAyudante());
				}
				case LAVAPLATOS -> {
					dto.setPuesto("Lavaplatos");
					dto.setAdicional(costo.getPlusLavaplatos());
				}
			}
			dto.setConceptoAdicional("Plus por categoría");
		} else if (empleado instanceof Cajero cajero) {
			// Antigüedad: años completos entre el ingreso y el fin del festival
			long anios = Math.max(0, ChronoUnit.YEARS.between(empleado.getFechaIngreso(), festival.getFechaFin()));
			dto.setPuesto("Cajero (turno " + (cajero.getTurno().name().equals("NOCHE") ? "noche" : "mañana") + ")");
			dto.setAdicional(costo.getMontoPorAnioAntiguedad().multiply(BigDecimal.valueOf(anios)));
			dto.setConceptoAdicional("Antigüedad (" + anios + (anios == 1 ? " año)" : " años)"));
		}

		dto.setSubtotal(dto.getSueldoBase().add(dto.getAdicional()));
		dto.setPorcentaje(calcularPorcentaje(dto.getDias()));
		dto.setTotal(calcularTotal(dto.getSubtotal(), dto.getDias()));
		return dto;
	}

	// Hasta 15 días: 50%. De 16 a 30: 100%. Más de 30: (int(días / 15) + 1) * 50%
	static int calcularPorcentaje(long dias) {
		if (dias <= 15) {
			return 50;
		}
		if (dias <= 30) {
			return 100;
		}
		return (int) ((dias / 15) + 1) * 50;
	}

	// Dinero en BigDecimal, redondeado a centavos
	static BigDecimal calcularTotal(BigDecimal subtotal, long dias) {
		return subtotal.multiply(BigDecimal.valueOf(calcularPorcentaje(dias)))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}
}
