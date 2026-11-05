package com.unla.epicentrogourmet.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.PedidoDTO;
import com.unla.epicentrogourmet.entities.Pedido;
import com.unla.epicentrogourmet.entities.Plato;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IPedidoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/empleado/pedidos")
@PreAuthorize("hasRole('EMPLEADO')")
public class PedidoController {

	private final IPedidoService pedidoService;

	public PedidoController(IPedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	// Lista pedidos de hoy por defecto. Las fechas y el plato son filtros opcionales.
	@GetMapping
	public ModelAndView pedidosDeHoy(
			Authentication authentication,
			@RequestParam(required = false) Integer creado,
			@RequestParam(required = false) Integer anulado,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
			@RequestParam(required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
			@RequestParam(required = false) Integer platoId,
			@PageableDefault(
					size = 10,
					sort = {"fecha", "id"},
					direction = Sort.Direction.DESC) Pageable pageable) {

		LocalDate hoy = LocalDate.now();

		// Si no se especifican fechas, el listado conserva el comportamiento actual:
		// mostrar los pedidos de hoy.
		if (desde == null && hasta == null) {
			desde = LocalDate.of(2026, 8, 1);
			hasta = hoy;
		}

		ModelAndView modelAndView = cargarListado(
				authentication,
				desde,
				hasta,
				platoId,
				pageable);

		modelAndView.addObject("creado", creado);
		modelAndView.addObject("anulado", anulado);

		return modelAndView;
	}

	// Anula un pedido de hoy cargado por error.
	@PostMapping("/{id}/anular")
	public ModelAndView anular(
			Authentication authentication,
			@PathVariable Integer id) {
		try {
			pedidoService.anular(authentication.getName(), id);

			// POST-Redirect-GET: al refrescar no se repite la anulación.
			return new ModelAndView(
					"redirect:" + ViewRouteHelper.PEDIDOS_ROOT + "?anulado=" + id);

		} catch (ValidacionException e) {
			LocalDate hoy = LocalDate.now();
			Pageable pageable = paginaDiariaPorDefecto();

			ModelAndView modelAndView = cargarListado(
					authentication,
					hoy,
					hoy,
					null,
					pageable);

			modelAndView.addObject("errorAnular", e.getMessage());

			return modelAndView;
		}
	}

	private ModelAndView cargarListado(
			Authentication authentication,
			LocalDate desde,
			LocalDate hasta,
			Integer platoId,
			Pageable pageable) {

		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.PEDIDO_LIST);

		modelAndView.addObject("hoy", LocalDate.now());
		modelAndView.addObject("desde", desde);
		modelAndView.addObject("hasta", hasta);
		modelAndView.addObject("platoId", platoId);

		try {
			String username = authentication.getName();

			modelAndView.addObject(
					"unidad",
					pedidoService.getUnidadDelCajero(username));

			modelAndView.addObject(
					"cajaCerrada",
					pedidoService.isCajaCerrada(username));

			List<Plato> platos =
					pedidoService.getPlatosParaFiltro(username);

			Page<Pedido> pagina = pedidoService.buscarPedidos(
					username,
					desde,
					hasta,
					platoId,
					pageable);

			BigDecimal totalPagina = pagina.getContent()
					.stream()
					.map(Pedido::getTotal)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			modelAndView.addObject("platosFiltro", platos);
			modelAndView.addObject("pagina", pagina);
			modelAndView.addObject("pedidos", pagina.getContent());
			modelAndView.addObject("totalPagina", totalPagina);

		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}

		return modelAndView;
	}

	private Pageable paginaDiariaPorDefecto() {
		Sort orden = Sort.by(Sort.Direction.DESC, "fecha")
				.and(Sort.by(Sort.Direction.DESC, "id"));

		return PageRequest.of(0, 10, orden);
	}

	// Muestra el cierre de caja del día de la unidad del cajero.
	@GetMapping("/cierre")
	public ModelAndView cierreDeCaja(Authentication authentication) {
		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.CIERRE_CAJA);

		try {
			modelAndView.addObject(
					"cierre",
					pedidoService.getCierreDeCaja(authentication.getName()));

		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}

		return modelAndView;
	}

	// Guarda el cierre de caja de hoy.
	@PostMapping("/cierre")
	public ModelAndView cerrarCaja(Authentication authentication) {
		try {
			pedidoService.cerrarCaja(authentication.getName());

		} catch (ValidacionException e) {
			ModelAndView modelAndView = cierreDeCaja(authentication);
			modelAndView.addObject("errorCerrar", e.getMessage());
			return modelAndView;

		} catch (DataIntegrityViolationException e) {
			// Otro cajero de la unidad cerró al mismo tiempo.
		}

		return new ModelAndView(
				"redirect:" + ViewRouteHelper.PEDIDOS_ROOT + "/cierre");
	}

	// Formulario con un renglón por plato activo.
	@GetMapping("/nuevo")
	public ModelAndView form(Authentication authentication) {
		try {
			return formConDatos(
					authentication,
					pedidoService.prepararFormulario(authentication.getName()));

		} catch (ValidacionException e) {
			return vistaError(e);
		}
	}

	// Registra el pedido completado.
	@PostMapping
	public ModelAndView registrar(
			Authentication authentication,
			@Valid @ModelAttribute("pedido") PedidoDTO pedido,
			BindingResult bindingResult) {
		try {
			if (bindingResult.hasErrors()) {
				return formConDatos(authentication, pedido);
			}

			Pedido creado = pedidoService.registrar(
					authentication.getName(),
					pedido);

			return new ModelAndView(
					"redirect:" + ViewRouteHelper.PEDIDOS_ROOT
							+ "?creado=" + creado.getId());

		} catch (ValidacionException e) {
			if (e.getCampo() == null) {
				return vistaError(e);
			}

			bindingResult.rejectValue(
					e.getCampo(),
					"error.pedido",
					e.getMessage());

			return formConDatos(authentication, pedido);
		}
	}

	private ModelAndView formConDatos(
			Authentication authentication,
			PedidoDTO pedido) {
		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.PEDIDO_FORM);

		modelAndView.addObject("pedido", pedido);
		modelAndView.addObject(
				"unidad",
				pedidoService.getUnidadDelCajero(authentication.getName()));
		modelAndView.addObject("hoy", LocalDate.now());

		return modelAndView;
	}

	private ModelAndView vistaError(ValidacionException e) {
		ModelAndView modelAndView =
				new ModelAndView(ViewRouteHelper.PEDIDO_FORM);

		modelAndView.addObject("errorGeneral", e.getMessage());

		return modelAndView;
	}
}