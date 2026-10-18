package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.ItemPedidoDTO;
import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Cajero;
import com.unla.epicentrogourmet.entities.DetallePedido;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.Plato;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IFestivalRepository;
import com.unla.epicentrogourmet.repositories.IPedidoRepository;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.repositories.IPlatoRepository;
import com.unla.epicentrogourmet.repositories.IUnidadDeVentaRepository;
import com.unla.epicentrogourmet.services.IPedidoService;

@Service("pedidoService")
public class PedidoService implements IPedidoService {

	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final IPersonalRepository personalRepository;
	private final IUnidadDeVentaRepository unidadDeVentaRepository;
	private final IFestivalRepository festivalRepository;
	private final IPlatoRepository platoRepository;
	private final IPedidoRepository pedidoRepository;

	public PedidoService(IPersonalRepository personalRepository, IUnidadDeVentaRepository unidadDeVentaRepository,
			IFestivalRepository festivalRepository, IPlatoRepository platoRepository, IPedidoRepository pedidoRepository) {
		this.personalRepository = personalRepository;
		this.unidadDeVentaRepository = unidadDeVentaRepository;
		this.festivalRepository = festivalRepository;
		this.platoRepository = platoRepository;
		this.pedidoRepository = pedidoRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean puedeCargarPedidos(String username) {
		// El tipo de empleado se sabe por la subclase (instanceof), no por un atributo
		return personalRepository.findByUsuarioUsername(username).filter(p -> p instanceof Cajero).isPresent();
	}

	@Override
	@Transactional(readOnly = true)
	public UnidadDeVenta getUnidadDelCajero(String username) {
		Personal empleado = personalRepository.findByUsuarioUsername(username)
				.orElseThrow(() -> new ValidacionException(null, "Tu usuario no corresponde a un empleado"));
		if (!(empleado instanceof Cajero)) {
			throw new ValidacionException(null, "Solo un cajero puede cargar pedidos");
		}
		return unidadDeVentaRepository.findByEmpleadoUsername(username)
				.orElseThrow(() -> new ValidacionException(null, "No estás asignado a ninguna unidad de venta"));
	}

	@Override
	@Transactional(readOnly = true)
	public PedidoDTO prepararFormulario(String username) {
		UnidadDeVenta unidad = getUnidadHabilitadaHoy(username);
		List<Plato> menu = platoRepository.findActivosByUnidadId(unidad.getId());
		if (menu.isEmpty()) {
			throw new ValidacionException(null, "Tu unidad no tiene platos activos para vender");
		}
		PedidoDTO dto = new PedidoDTO();
		menu.forEach(plato -> dto.getItems().add(new ItemPedidoDTO(plato.getId(), plato.getNombre(), plato.getPrecio())));
		return dto;
	}

	// Todo o nada: si un renglón no es válido, no se guarda ninguna parte del pedido
	@Override
	@Transactional
	public Pedido registrar(String username, PedidoDTO dto) {
		UnidadDeVenta unidad = getUnidadHabilitadaHoy(username);

		// Precio y plato salen de la base, no del formulario: lo que manda el navegador se puede adulterar
		Map<Integer, Plato> menu = platoRepository.findActivosByUnidadId(unidad.getId()).stream()
				.collect(Collectors.toMap(Plato::getId, Function.identity()));

		// Si el mismo plato llega dos veces, se suman las cantidades en un solo renglón
		Map<Integer, Integer> cantidades = new LinkedHashMap<>();
		for (ItemPedidoDTO item : dto.getItems()) {
			if (item.isPedido()) {
				if (!menu.containsKey(item.getPlatoId())) {
					throw new ValidacionException("items", "Uno de los platos no está disponible en tu unidad");
				}
				cantidades.merge(item.getPlatoId(), item.getCantidad(), Integer::sum);
			}
		}
		if (cantidades.isEmpty()) {
			throw new ValidacionException("items", "Cargá al menos un plato con cantidad mayor a 0");
		}

		Pedido pedido = new Pedido();
		pedido.setFecha(LocalDate.now()); // validación de carga: el pedido solo puede tener la fecha de hoy
		pedido.setUnidad(unidad);         // el pedido es el dueño de la FK unidad_id
		BigDecimal total = BigDecimal.ZERO;
		for (Map.Entry<Integer, Integer> renglon : cantidades.entrySet()) {
			Plato plato = menu.get(renglon.getKey());
			DetallePedido detalle = new DetallePedido();
			detalle.setPlato(plato);
			detalle.setCantidad(renglon.getValue());
			pedido.agregarDetalle(detalle); // setea detalle.pedido y lo agrega a la lista
			total = total.add(plato.getPrecio().multiply(BigDecimal.valueOf(renglon.getValue())));
		}
		pedido.setTotal(total);

		// Como en el ejemplo de la cátedra: el hijo se guarda con su propio repositorio.
		// Es nuevo, así que save() hace persist (inserta este mismo objeto y le asigna el id);
		// cascade = ALL guarda también sus renglones
		return pedidoRepository.save(pedido);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Pedido> getPedidosDeHoy(String username) {
		UnidadDeVenta unidad = getUnidadDelCajero(username);
		return pedidoRepository.findByUnidadIdAndFecha(unidad.getId(), LocalDate.now());
	}

	// La unidad del cajero, siempre que su festival esté activo hoy
	private UnidadDeVenta getUnidadHabilitadaHoy(String username) {
		UnidadDeVenta unidad = getUnidadDelCajero(username);
		Festival festival = festivalRepository.findByUnidadId(unidad.getId())
				.orElseThrow(() -> new ValidacionException(null, "Tu unidad no está asignada a ningún festival"));
		LocalDate hoy = LocalDate.now();
		if (hoy.isBefore(festival.getFechaInicio()) || hoy.isAfter(festival.getFechaFin())) {
			throw new ValidacionException(null, "Hoy no hay un festival activo para tu unidad: " + festival.getNombre()
					+ " es del " + festival.getFechaInicio().format(FORMATO_FECHA)
					+ " al " + festival.getFechaFin().format(FORMATO_FECHA));
		}
		return unidad;
	}
}
