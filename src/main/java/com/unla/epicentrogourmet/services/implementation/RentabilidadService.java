package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.dtos.RentabilidadDTO;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPedidoRepository;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.services.ILiquidacionService;
import com.unla.epicentrogourmet.services.IRentabilidadService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("rentabilidadService")
public class RentabilidadService implements IRentabilidadService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final IUnidadDeVentaService unidadDeVentaService;
	private final IPedidoRepository pedidoRepository;
	private final IPersonalRepository personalRepository;
	private final ILiquidacionService liquidacionService;

	public RentabilidadService(IUnidadDeVentaService unidadDeVentaService, IPedidoRepository pedidoRepository,
			IPersonalRepository personalRepository, ILiquidacionService liquidacionService) {
		this.unidadDeVentaService = unidadDeVentaService;
		this.pedidoRepository = pedidoRepository;
		this.personalRepository = personalRepository;
		this.liquidacionService = liquidacionService;
	}

	// (ventas - costo de los platos) - sueldos - canon. Sin fechas va del primer día del festival a hoy
	// (si ya terminó, hasta su último día). Antes de que empiece no hay nada que medir
	@Override
	@Transactional(readOnly = true)
	public RentabilidadDTO calcular(String username, LocalDate desde, LocalDate hasta) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		Festival festival = unidad.getFestival();
		LocalDate hoy = LocalDate.now();
		if (hoy.isBefore(festival.getFechaInicio())) {
			throw new ValidacionException(null, "El festival empieza el "
					+ festival.getFechaInicio().format(FORMATO_FECHA) + ": todavía no hay rentabilidad para calcular");
		}
		LocalDate ultimoDiaTranscurrido = hoy.isAfter(festival.getFechaFin()) ? festival.getFechaFin() : hoy;
		LocalDate inicio = desde == null ? festival.getFechaInicio() : desde;
		LocalDate fin = hasta == null ? ultimoDiaTranscurrido : hasta;
		validarPeriodo(festival, inicio, fin);

		RentabilidadDTO dto = new RentabilidadDTO();
		dto.setUnidad(unidad.getNombreComercial());
		dto.setFestival(festival.getNombre());
		dto.setDesde(inicio);
		dto.setHasta(fin);
		dto.setDias(ChronoUnit.DAYS.between(inicio, fin) + 1);
		dto.setDiasFestival(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
		dto.setDiasTranscurridos(ChronoUnit.DAYS.between(festival.getFechaInicio(), ultimoDiaTranscurrido) + 1);

		// Ventas y costo de platos: solo los pedidos del período
		dto.setVentas(ceroSiNulo(pedidoRepository.sumVentas(unidad.getId(), inicio, fin)));
		dto.setCostoPlatos(ceroSiNulo(pedidoRepository.sumCostoPlatos(unidad.getId(), inicio, fin)));
		dto.setGananciaBruta(dto.getVentas().subtract(dto.getCostoPlatos()));

		// Sueldos: la misma cuenta que el cierre de liquidación. La escala va por bloques de días acumulados,
		// así que el sueldo de un período es lo que corresponde al terminar menos lo que correspondía antes
		// de empezar. Si esos días no entran en un bloque nuevo, el período da 0
		BigDecimal sueldos = BigDecimal.ZERO;
		for (Personal empleado : personalRepository.findByUnidadIdAndUsuarioEnabledTrue(unidad.getId())) {
			BigDecimal antesDelPeriodo = empleado.calcularSueldoAcumulado(festival, inicio.minusDays(1));
			LiquidacionHaberesDTO liquidacion = liquidacionService.calcularCierre(empleado, inicio, fin, antesDelPeriodo);
			dto.getLiquidaciones().add(liquidacion);
			sueldos = sueldos.add(liquidacion.getTotal());
		}
		dto.setSueldos(sueldos);
		// El canon se paga por el festival entero: en un período más corto se toma la parte proporcional a
		// sus días, para comparar con las ventas de esos mismos días
		dto.setCanon(proporcional(unidad.calcularCanon(festival.getCosto()), dto.getDias(), dto.getDiasFestival()));

		dto.setRentabilidadNeta(dto.getGananciaBruta().subtract(dto.getSueldos()).subtract(dto.getCanon()));
		return dto;
	}

	// Validaciones de carga del período: en orden y dentro de las fechas del festival
	private void validarPeriodo(Festival festival, LocalDate desde, LocalDate hasta) {
		if (desde.isAfter(hasta)) {
			throw new ValidacionException("desde", "La fecha desde no puede ser posterior a la fecha hasta");
		}
		if (desde.isBefore(festival.getFechaInicio()) || hasta.isAfter(festival.getFechaFin())) {
			throw new ValidacionException("desde", "Las fechas tienen que estar dentro del festival: del "
					+ festival.getFechaInicio().format(FORMATO_FECHA) + " al " + festival.getFechaFin().format(FORMATO_FECHA));
		}
	}

	private BigDecimal proporcional(BigDecimal monto, long dias, long diasTotales) {
		return monto.multiply(BigDecimal.valueOf(dias)).divide(BigDecimal.valueOf(diasTotales), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal ceroSiNulo(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}
}
