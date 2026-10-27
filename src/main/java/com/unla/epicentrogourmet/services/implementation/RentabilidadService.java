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

	// (ventas - costo de los platos) - sueldos - canon. El festival completo es el caso particular
	// en que el período va del primer al último día: sueldos y canon entran enteros
	@Override
	@Transactional(readOnly = true)
	public RentabilidadDTO calcular(String username, LocalDate desde, LocalDate hasta) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		Festival festival = unidad.getFestival();
		LocalDate inicio = desde == null ? festival.getFechaInicio() : desde;
		LocalDate fin = hasta == null ? festival.getFechaFin() : hasta;
		validarPeriodo(festival, inicio, fin);

		RentabilidadDTO dto = new RentabilidadDTO();
		dto.setUnidad(unidad.getNombreComercial());
		dto.setFestival(festival.getNombre());
		dto.setDesde(inicio);
		dto.setHasta(fin);
		dto.setDias(ChronoUnit.DAYS.between(inicio, fin) + 1);
		dto.setDiasFestival(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);

		// Ventas y costo de platos: solo los pedidos del período
		dto.setVentas(ceroSiNulo(pedidoRepository.sumVentas(unidad.getId(), inicio, fin)));
		dto.setCostoPlatos(ceroSiNulo(pedidoRepository.sumCostoPlatos(unidad.getId(), inicio, fin)));
		dto.setGananciaBruta(dto.getVentas().subtract(dto.getCostoPlatos()));

		// Sueldos: la liquidación de cada empleado activo, con la misma cuenta que ve el empleado
		BigDecimal sueldosFestival = BigDecimal.ZERO;
		for (Personal empleado : personalRepository.findByUnidadIdAndUsuarioEnabledTrue(unidad.getId())) {
			LiquidacionHaberesDTO liquidacion = liquidacionService.calcular(empleado);
			dto.getLiquidaciones().add(liquidacion);
			sueldosFestival = sueldosFestival.add(liquidacion.getTotal());
		}
		// Sueldos y canon se pagan por el festival entero: en un período más corto se toma la parte
		// proporcional a sus días, para comparar con las ventas de esos mismos días
		dto.setSueldos(proporcional(sueldosFestival, dto.getDias(), dto.getDiasFestival()));
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
