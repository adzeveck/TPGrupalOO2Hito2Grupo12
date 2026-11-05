package com.unla.epicentrogourmet.services.implementation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

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
	// personal de una unidad y la rentabilidad neta (que resta los sueldos). Cubre el festival completo
	@Override
	@Transactional(readOnly = true)
	public LiquidacionHaberesDTO calcular(Personal empleado) {
		Festival festival = festivalDe(empleado);
		return armar(empleado, festival, festival.getFechaInicio(), festival.getFechaFin());
	}

	// Lo que se paga al cerrar un tramo: la escala se aplica a los días acumulados desde el primer día del
	// empleado (no a los del tramo) y se resta lo ya pagado. Así, se cierre como se cierre, la suma da lo mismo
	@Override
	@Transactional(readOnly = true)
	public LiquidacionHaberesDTO calcularCierre(Personal empleado, LocalDate desde, LocalDate hasta,
												BigDecimal yaPagado) {
		Festival festival = festivalDe(empleado);
		if (desde.isAfter(hasta)) {
			throw new ValidacionException(null, "El período no es válido: empieza después de terminar");
		}
		if (desde.isBefore(festival.getFechaInicio()) || hasta.isAfter(festival.getFechaFin())) {
			throw new ValidacionException(null, "El período tiene que estar dentro del festival");
		}
		LiquidacionHaberesDTO dto = armar(empleado, festival, desde, hasta);   // datos y días del tramo
		long diasAcumulados = empleado.calcularDiasAcumulados(festival, hasta);
		BigDecimal corresponde = empleado.calcularSueldoAcumulado(festival, hasta);
		dto.setDiasAcumulados(diasAcumulados);
		dto.setPorcentaje(empleado.calcularPorcentajeLiquidacion(diasAcumulados));
		dto.setCorresponde(corresponde);
		dto.setYaPagado(yaPagado);
		// Nunca negativo: si por un cambio de costos ya se pagó de más, este cierre paga 0
		dto.setTotal(corresponde.subtract(yaPagado).max(BigDecimal.ZERO));
		return dto;
	}

	private Festival festivalDe(Personal empleado) {
		UnidadDeVenta unidad = empleado.getUnidad();
		if (unidad == null) {
			throw new ValidacionException(null, "El empleado no está asignado a ninguna unidad de venta");
		}
		return unidad.getFestival();
	}

	// Las cuentas las hace el empleado (calcularAdicional, calcularSueldo...); acá solo se arma el DTO
	private LiquidacionHaberesDTO armar(Personal empleado, Festival festival, LocalDate desde, LocalDate hasta) {
		Costo costo = festival.getCosto(); // los montos salen de los costos del festival: nada hardcodeado
		long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;

		LiquidacionHaberesDTO dto = new LiquidacionHaberesDTO();
		dto.setEmpleadoId(empleado.getId());
		dto.setEmpleado(empleado.getNombre() + " " + empleado.getApellido());
		dto.setDni(empleado.getDni());
		dto.setUnidad(empleado.getUnidad().getNombreComercial());
		dto.setFestival(festival.getNombre());
		dto.setFechaInicio(desde);
		dto.setFechaFin(hasta);
		dto.setDias(dias);
		dto.setSueldoBase(costo.getSueldoBase());
		dto.setAdicional(empleado.calcularAdicional(costo, hasta));
		dto.setSubtotal(dto.getSueldoBase().add(dto.getAdicional()));
		dto.setPorcentaje(empleado.calcularPorcentajeLiquidacion(dias));
		dto.setTotal(empleado.calcularSueldo(costo, hasta, dias));
		dto.setDiasAcumulados(dias);
		dto.setCorresponde(dto.getTotal());
		dto.setYaPagado(BigDecimal.ZERO);

		// Los textos para mostrar dependen del tipo de empleado (la subclase), no de un atributo
		if (empleado instanceof Cocinero cocinero) {
			dto.setPuesto(switch (cocinero.getCategoria()) {
				case COCINERO -> "Cocinero";
				case AYUDANTE -> "Ayudante de cocina";
				case LAVAPLATOS -> "Lavaplatos";
			});
			dto.setConceptoAdicional("Plus por categoría");
		} else if (empleado instanceof Cajero cajero) {
			long anios = empleado.calcularAniosDeAntiguedad(hasta);
			dto.setPuesto("Cajero (turno " + (cajero.getTurno().name().equals("NOCHE") ? "noche" : "mañana") + ")");
			dto.setConceptoAdicional("Antigüedad (" + anios + (anios == 1 ? " año)" : " años)"));
		}
		return dto;
	}
}