package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.CierreCajaDTO;
import com.unla.epicentrogourmet.dtos.ItemPedidoDTO;
import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Cajero;
import com.unla.epicentrogourmet.entities.CierreCaja;
import com.unla.epicentrogourmet.entities.DetallePedido;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.Plato;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.enums.Turno;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.ICierreCajaRepository;
import com.unla.epicentrogourmet.repositories.IPedidoRepository;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.repositories.IPlatoRepository;
import com.unla.epicentrogourmet.services.IPedidoService;

@Service("pedidoService")
public class PedidoService implements IPedidoService {

	private static final DateTimeFormatter FORMATO_FECHA =
			DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final IPersonalRepository personalRepository;
	private final IPlatoRepository platoRepository;
	private final IPedidoRepository pedidoRepository;
	private final ICierreCajaRepository cierreCajaRepository;

	public PedidoService(
			IPersonalRepository personalRepository,
			IPlatoRepository platoRepository,
			IPedidoRepository pedidoRepository,
			ICierreCajaRepository cierreCajaRepository) {
		this.personalRepository = personalRepository;
		this.platoRepository = platoRepository;
		this.pedidoRepository = pedidoRepository;
		this.cierreCajaRepository = cierreCajaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean puedeCargarPedidos(String username) {
		return personalRepository.findByUsuarioUsername(username)
				.filter(personal -> personal instanceof Cajero)
				.isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public UnidadDeVenta getUnidadDelCajero(String username) {
		return getCajero(username).getUnidad();
	}

	@Override
	@Transactional(readOnly = true)
	public PedidoDTO prepararFormulario(String username) {
		UnidadDeVenta unidad = getUnidadHabilitadaHoy(username);
		validarCajaAbierta(unidad);

		List<Plato> menu =
				platoRepository.findByUnidadIdAndActivoTrueOrderByNombre(unidad.getId());

		if (menu.isEmpty()) {
			throw new ValidacionException(
					null,
					"Tu unidad no tiene platos activos para vender");
		}

		PedidoDTO dto = new PedidoDTO();
		menu.forEach(plato -> dto.getItems().add(
				new ItemPedidoDTO(
						plato.getId(),
						plato.getNombre(),
						plato.getPrecio())));

		return dto;
	}

	// Guarda el pedido completo o no guarda ninguna parte.
	@Override
	@Transactional
	public Pedido registrar(String username, PedidoDTO dto) {
		UnidadDeVenta unidad = getUnidadHabilitadaHoy(username);
		validarCajaAbierta(unidad);

		// El precio y los platos válidos se consultan en la base.
		Map<Integer, Plato> menu =
				platoRepository.findByUnidadIdAndActivoTrueOrderByNombre(unidad.getId())
						.stream()
						.collect(Collectors.toMap(
								Plato::getId,
								Function.identity()));

		// Si un plato llega repetido, se unifican sus cantidades.
		Map<Integer, Integer> cantidades = new LinkedHashMap<>();

		for (ItemPedidoDTO item : dto.getItems()) {
			if (item.isPedido()) {
				if (!menu.containsKey(item.getPlatoId())) {
					throw new ValidacionException(
							"items",
							"Uno de los platos no está disponible en tu unidad");
				}

				cantidades.merge(
						item.getPlatoId(),
						item.getCantidad(),
						Integer::sum);
			}
		}

		if (cantidades.isEmpty()) {
			throw new ValidacionException(
					"items",
					"Cargá al menos un plato con cantidad mayor a 0");
		}

		Pedido pedido = new Pedido();
		pedido.setFecha(LocalDate.now());
		pedido.setUnidad(unidad);

		BigDecimal total = BigDecimal.ZERO;

		for (Map.Entry<Integer, Integer> renglon : cantidades.entrySet()) {
			Plato plato = menu.get(renglon.getKey());

			DetallePedido detalle = new DetallePedido();
			detalle.setPlato(plato);
			detalle.setCantidad(renglon.getValue());
			pedido.agregarDetalle(detalle);

			total = total.add(
					plato.getPrecio().multiply(
							BigDecimal.valueOf(renglon.getValue())));
		}

		pedido.setTotal(total);

		return pedidoRepository.save(pedido);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pedido> getPedidosDeHoy(String username) {
		UnidadDeVenta unidad = getUnidadDelCajero(username);

		return pedidoRepository.findByUnidadIdAndFecha(
				unidad.getId(),
				LocalDate.now());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Pedido> buscarPedidos(
			String username,
			LocalDate desde,
			LocalDate hasta,
			Integer platoId,
			Pageable pageable) {

		if (desde != null && hasta != null && desde.isAfter(hasta)) {
			throw new ValidacionException(
					null,
					"La fecha desde no puede ser posterior a la fecha hasta");
		}

		UnidadDeVenta unidad = getUnidadDelCajero(username);

		Page<Integer> paginaIds = pedidoRepository.buscarIdsDeUnidad(
				unidad.getId(),
				desde,
				hasta,
				platoId,
				pageable);

		if (paginaIds.getContent().isEmpty()) {
			return new PageImpl<>(
					List.of(),
					pageable,
					paginaIds.getTotalElements());
		}

		List<Pedido> pedidosCargados =
				pedidoRepository.buscarPedidosConDetallesPorIds(
						paginaIds.getContent());

		Map<Integer, Pedido> pedidosPorId = pedidosCargados.stream()
				.collect(Collectors.toMap(
						Pedido::getId,
						Function.identity()));

		List<Pedido> pedidosEnOrden = paginaIds.getContent()
				.stream()
				.map(pedidosPorId::get)
				.filter(pedido -> pedido != null)
				.collect(Collectors.toList());

		return new PageImpl<>(
				pedidosEnOrden,
				pageable,
				paginaIds.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Plato> getPlatosParaFiltro(String username) {
		UnidadDeVenta unidad = getUnidadDelCajero(username);

		return platoRepository.findByUnidadIdOrderByNombreAsc(unidad.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public CierreCajaDTO getCierreDeCaja(String username) {
		Cajero cajero = getCajero(username);
		LocalDate hoy = LocalDate.now();

		List<Pedido> pedidos = pedidoRepository.findByUnidadIdAndFecha(
				cajero.getUnidad().getId(),
				hoy);

		CierreCajaDTO cierre = new CierreCajaDTO();
		cierre.setFecha(hoy);
		cierre.setUnidad(cajero.getUnidad().getNombreComercial());
		cierre.setCajero(describir(cajero));
		cierre.setCantidadPedidos(pedidos.size());
		cierre.setTotal(sumarTotales(pedidos));

		cierreCajaRepository.findByUnidadIdAndFecha(
				cajero.getUnidad().getId(),
				hoy).ifPresent(guardado -> {
			cierre.setCerrada(true);
			cierre.setCerradoPor(describir(guardado.getCajero()));
		});

		cierre.setPuedeCerrar(
				!cierre.isCerrada()
						&& hayFestivalActivo(cajero.getUnidad().getFestival()));

		Map<String, Integer> unidades = new HashMap<>();

		for (Pedido pedido : pedidos) {
			for (DetallePedido detalle : pedido.getDetalles()) {
				unidades.merge(
						detalle.getPlato().getNombre(),
						detalle.getCantidad(),
						Integer::sum);
			}
		}

		unidades.entrySet()
				.stream()
				.sorted(
						Map.Entry.<String, Integer>comparingByValue()
								.reversed()
								.thenComparing(Map.Entry.comparingByKey()))
				.forEach(renglon -> cierre.getUnidadesPorPlato().put(
						renglon.getKey(),
						renglon.getValue()));

		return cierre;
	}

	// Una vez cerrada la caja, ningún cajero de la unidad puede cargar pedidos.
	@Override
	@Transactional
	public void cerrarCaja(String username) {
		Cajero cajero = getCajero(username);
		UnidadDeVenta unidad = getUnidadHabilitadaHoy(username);
		validarCajaAbierta(unidad);

		LocalDate hoy = LocalDate.now();
		List<Pedido> pedidos =
				pedidoRepository.findByUnidadIdAndFecha(unidad.getId(), hoy);

		CierreCaja cierre = new CierreCaja();
		cierre.setFecha(hoy);
		cierre.setUnidad(unidad);
		cierre.setCajero(cajero);
		cierre.setCantidadPedidos(pedidos.size());
		cierre.setTotal(sumarTotales(pedidos));

		cierreCajaRepository.save(cierre);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isCajaCerrada(String username) {
		return personalRepository.findByUsuarioUsername(username)
				.map(Personal::getUnidad)
				.filter(this::estaCerrada)
				.isPresent();
	}

	// Solo se pueden anular pedidos de hoy y antes de cerrar la caja.
	@Override
	@Transactional
	public void anular(String username, Integer pedidoId) {
		Cajero cajero = getCajero(username);

		Pedido pedido = pedidoRepository.findByIdAndUnidadId(
						pedidoId,
						cajero.getUnidad().getId())
				.orElseThrow(() -> new ValidacionException(
						null,
						"El pedido no pertenece a tu unidad de venta"));

		if (!pedido.getFecha().equals(LocalDate.now())) {
			throw new ValidacionException(
					null,
					"Solo se pueden anular pedidos de hoy: el pedido #"
							+ pedido.getId()
							+ " es del "
							+ pedido.getFecha().format(FORMATO_FECHA));
		}

		validarCajaAbierta(cajero.getUnidad());
		pedidoRepository.delete(pedido);
	}

	// Devuelve el empleado logueado solo si es cajero y tiene una unidad.
	private Cajero getCajero(String username) {
		Personal empleado = personalRepository.findByUsuarioUsername(username)
				.orElseThrow(() -> new ValidacionException(
						null,
						"Tu usuario no corresponde a un empleado"));

		if (!(empleado instanceof Cajero cajero)) {
			throw new ValidacionException(
					null,
					"Solo un cajero puede cargar pedidos y cerrar la caja");
		}

		if (cajero.getUnidad() == null) {
			throw new ValidacionException(
					null,
					"No estás asignado a ninguna unidad de venta");
		}

		return cajero;
	}

	// La unidad del cajero, siempre que su festival esté activo hoy.
	private UnidadDeVenta getUnidadHabilitadaHoy(String username) {
		UnidadDeVenta unidad = getUnidadDelCajero(username);
		Festival festival = unidad.getFestival();

		if (!hayFestivalActivo(festival)) {
			throw new ValidacionException(
					null,
					"Hoy no hay un festival activo para tu unidad: "
							+ festival.getNombre()
							+ " es del "
							+ festival.getFechaInicio().format(FORMATO_FECHA)
							+ " al "
							+ festival.getFechaFin().format(FORMATO_FECHA));
		}

		return unidad;
	}

	private boolean hayFestivalActivo(Festival festival) {
		LocalDate hoy = LocalDate.now();

		return !hoy.isBefore(festival.getFechaInicio())
				&& !hoy.isAfter(festival.getFechaFin());
	}

	private boolean estaCerrada(UnidadDeVenta unidad) {
		return cierreCajaRepository.findByUnidadIdAndFecha(
				unidad.getId(),
				LocalDate.now()).isPresent();
	}

	// La validación también se hace en el servidor, no solo ocultando los botones.
	private void validarCajaAbierta(UnidadDeVenta unidad) {
		if (estaCerrada(unidad)) {
			throw new ValidacionException(
					null,
					"La caja de hoy ya está cerrada: "
							+ "no se pueden cargar ni anular pedidos");
		}
	}

	private BigDecimal sumarTotales(List<Pedido> pedidos) {
		return pedidos.stream()
				.map(Pedido::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private String describir(Cajero cajero) {
		return cajero.getNombre()
				+ " "
				+ cajero.getApellido()
				+ " (turno "
				+ (cajero.getTurno() == Turno.NOCHE ? "noche" : "mañana")
				+ ")";
	}
}