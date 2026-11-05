package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.dtos.PlatoDTO;
import com.unla.epicentrogourmet.dtos.RecaudacionDiariaDTO;
import com.unla.epicentrogourmet.dtos.RentabilidadDTO;
import com.unla.epicentrogourmet.dtos.ReporteVentaDTO;
import com.unla.epicentrogourmet.dtos.TableroResponsableDTO;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.enums.EstadoFestival;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.services.IEmpleadoService;
import com.unla.epicentrogourmet.services.ILiquidacionPersonalService;
import com.unla.epicentrogourmet.services.IRentabilidadService;
import com.unla.epicentrogourmet.services.IReporteService;
import com.unla.epicentrogourmet.services.ITableroService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

// Junta en una pantalla lo que ya calculan los servicios de cada reporte: no repite ninguna cuenta
// ni modifica esos servicios. Cada link del tablero lleva a la pantalla de detalle que ya existía
@Service("tableroService")
public class TableroService implements ITableroService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final IUnidadDeVentaService unidadDeVentaService;
	private final IReporteService reporteService;
	private final IRentabilidadService rentabilidadService;
	private final IEmpleadoService empleadoService;
	private final ILiquidacionPersonalService liquidacionPersonalService;

	public TableroService(IUnidadDeVentaService unidadDeVentaService, IReporteService reporteService,
			IRentabilidadService rentabilidadService, IEmpleadoService empleadoService,
			ILiquidacionPersonalService liquidacionPersonalService) {
		this.unidadDeVentaService = unidadDeVentaService;
		this.reporteService = reporteService;
		this.rentabilidadService = rentabilidadService;
		this.empleadoService = empleadoService;
		this.liquidacionPersonalService = liquidacionPersonalService;
	}

	// Sin @Transactional a propósito: cada servicio que se llama abre y cierra su propia transacción.
	// Si este método tuviera una, una ValidacionException adentro de otro servicio (por ejemplo, "todavía
	// no hay pedidos") la marcaría para rollback y todo el tablero fallaría aunque la atrapemos acá
	@Override
	public TableroResponsableDTO getTablero(String username, LocalDate hoy) {
		// Sin unidad no hay tablero: esta excepción sí sube y la muestra el controller
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		Festival festival = unidad.getFestival();

		TableroResponsableDTO tablero = new TableroResponsableDTO();
		tablero.setUnidad(unidad.getNombreComercial());
		tablero.setCodigo(unidad.getCodigo());
		tablero.setTipo(unidad.getTipo());
		cargarFestival(tablero, festival, hoy);

		// Cada indicador se carga por separado: si uno no tiene datos, los demás se muestran igual
		cargarRecaudacion(tablero, username);
		cargarPedidosDeHoy(tablero, username, hoy);
		cargarCanon(tablero, username);
		cargarPersonal(tablero, username);
		cargarRentabilidad(tablero, username, festival, hoy);
		cargarPlatoEstrella(tablero, username);
		return tablero;
	}

	// Días contados igual que en la rentabilidad y la liquidación: el primer y el último día cuentan
	private void cargarFestival(TableroResponsableDTO tablero, Festival festival, LocalDate hoy) {
		LocalDate inicio = festival.getFechaInicio();
		LocalDate fin = festival.getFechaFin();
		EstadoFestival estado = EstadoFestival.de(inicio, fin, hoy);
		long total = ChronoUnit.DAYS.between(inicio, fin) + 1;
		long transcurridos = switch (estado) {
			case POR_VENIR -> 0;
			case EN_CURSO -> ChronoUnit.DAYS.between(inicio, hoy) + 1;
			case TERMINADO -> total;
		};

		tablero.setFestival(festival.getNombre());
		tablero.setFechaInicio(inicio);
		tablero.setFechaFin(fin);
		tablero.setEstado(estado);
		tablero.setDiasTotales(total);
		tablero.setDiasTranscurridos(transcurridos);
		tablero.setDiasRestantes(total - transcurridos);
		tablero.setDiasParaEmpezar(estado == EstadoFestival.POR_VENIR ? ChronoUnit.DAYS.between(hoy, inicio) : 0);
		tablero.setPorcentajeAvance((int) (transcurridos * 100 / total));
	}

	private void cargarRecaudacion(TableroResponsableDTO tablero, String username) {
		try {
			ReporteVentaDTO venta = reporteService.getRecaudacionDeMiUnidad(username);
			tablero.setRecaudacion(ceroSiNulo(venta.getRecaudacion()));
			tablero.setCantidadPedidos(venta.getCantidadPedidos());
		} catch (ValidacionException e) {
			// Queda en null: la tarjeta muestra "Sin datos"
		}
	}

	// Sale de la recaudación por día: el día de hoy, si ya hay pedidos cargados
	private void cargarPedidosDeHoy(TableroResponsableDTO tablero, String username, LocalDate hoy) {
		try {
			RecaudacionDiariaDTO deHoy = reporteService.getRecaudacionDiaria(username).stream()
					.filter(dia -> dia.getFecha().equals(hoy))
					.findFirst()
					.orElse(null);
			tablero.setPedidosHoy(deHoy == null ? 0L : deHoy.getCantidadPedidos());
			tablero.setRecaudacionHoy(deHoy == null ? BigDecimal.ZERO : ceroSiNulo(deHoy.getRecaudacion()));
		} catch (ValidacionException e) {
			// Queda en null
		}
	}

	private void cargarCanon(TableroResponsableDTO tablero, String username) {
		try {
			tablero.setCanon(unidadDeVentaService.calcularCanon(username));
		} catch (ValidacionException e) {
			// Queda en null
		}
	}

	private void cargarPersonal(TableroResponsableDTO tablero, String username) {
		try {
			// Sin filtros: todos los empleados activos (los dados de baja no cuentan)
			tablero.setEmpleadosActivos(empleadoService.listarActivos(username, null, null, null, null, null).size());
			tablero.setSueldosALiquidar(liquidacionPersonalService.calcular(username).getTotalPlanilla());
		} catch (ValidacionException e) {
			// Quedan en null
		}
	}

	// Del primer día del festival a hoy. Si ya terminó, hasta el último día; si no empezó, no hay período
	private void cargarRentabilidad(TableroResponsableDTO tablero, String username, Festival festival, LocalDate hoy) {
		if (tablero.getEstado() == EstadoFestival.POR_VENIR) {
			tablero.setMensajeRentabilidad("El festival empieza el " + festival.getFechaInicio().format(FORMATO_FECHA));
			return;
		}
		LocalDate hasta = hoy.isAfter(festival.getFechaFin()) ? festival.getFechaFin() : hoy;
		try {
			RentabilidadDTO rentabilidad = rentabilidadService.calcular(username, festival.getFechaInicio(), hasta);
			tablero.setRentabilidad(rentabilidad.getRentabilidadNeta());
			tablero.setRentabilidadHasta(hasta);
		} catch (ValidacionException e) {
			tablero.setMensajeRentabilidad(e.getMessage());
		}
	}

	private void cargarPlatoEstrella(TableroResponsableDTO tablero, String username) {
		try {
			PlatoDTO plato = reporteService.getPlatoEstrella(username);
			tablero.setPlatoEstrella(plato.getNombre());
			tablero.setPlatoEstrellaCantidad(plato.getCantidad());
		} catch (ValidacionException e) {
			tablero.setMensajePlatoEstrella(e.getMessage()); // "Tu unidad todavía no tiene pedidos cargados"
		}
	}

	private BigDecimal ceroSiNulo(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}
}
