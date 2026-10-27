package com.unla.epicentrogourmet.controllers;

import java.time.LocalDate;

import com.unla.epicentrogourmet.services.IEmpleadoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.entities.FoodTruck;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.dtos.EmpleadoFormDTO;
import com.unla.epicentrogourmet.enums.CategoriaCocina;
import com.unla.epicentrogourmet.enums.Turno;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IRentabilidadService;
import com.unla.epicentrogourmet.services.IReporteService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Controller
@RequestMapping("/responsable")
@PreAuthorize("hasRole('RESPONSABLE')")
public class ResponsableController {

	private final IUnidadDeVentaService unidadDeVentaService;
	private final IReporteService reporteService;
	private final IRentabilidadService rentabilidadService;
	private final IEmpleadoService empleadoService;
	public ResponsableController(IUnidadDeVentaService unidadDeVentaService, IReporteService reporteService,
                                 IRentabilidadService rentabilidadService, IEmpleadoService empleadoService) {
		this.unidadDeVentaService = unidadDeVentaService;
		this.reporteService = reporteService;
		this.rentabilidadService = rentabilidadService;
        this.empleadoService = empleadoService;
    }

	@GetMapping("/index")
	public ModelAndView index() {
		return new ModelAndView(ViewRouteHelper.RESPONSABLE_INDEX);
	}

	// Canon de la unidad del responsable logueado. La fórmula la resuelve la propia unidad
	@GetMapping("/canon")
	public ModelAndView canon(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_CANON);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("unidad", unidad);
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("canon", unidadDeVentaService.calcularCanon(authentication.getName()));
			// La vista muestra la fórmula que corresponde al tipo de unidad
			modelAndView.addObject("esFoodTruck", unidad instanceof FoodTruck);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Plato estrella: el más pedido de la unidad del responsable
	@GetMapping("/plato-estrella")
	public ModelAndView platoEstrella(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_PLATO_ESTRELLA);
		try {
			modelAndView.addObject("plato", reporteService.getPlatoEstrella(authentication.getName()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Recaudación de la unidad del responsable en su festival, y el total del festival como referencia
	@GetMapping("/recaudacion")
	public ModelAndView recaudacion(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_RECAUDACION);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("reporte", reporteService.getRecaudacionDeMiUnidad(authentication.getName()));
			modelAndView.addObject("totalFestival", reporteService.getRecaudacionTotal(unidad.getFestival().getId()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Rentabilidad neta: sin fechas, el festival completo; con fechas, ese período
	@GetMapping("/rentabilidad")
	public ModelAndView rentabilidad(Authentication authentication,
									 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
									 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_RENTABILIDAD);
		try {
			// El festival se pasa siempre: el formulario usa sus fechas como límites
			modelAndView.addObject("festival", unidadDeVentaService.getDelResponsable(authentication.getName()).getFestival());
			modelAndView.addObject("desde", desde);
			modelAndView.addObject("hasta", hasta);
			modelAndView.addObject("rentabilidad", rentabilidadService.calcular(authentication.getName(), desde, hasta));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}

	// Recaudación de la unidad del responsable día por día
	@GetMapping("/recaudacion/diaria")
	public ModelAndView recaudacionDiaria(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.RESPONSABLE_RECAUDACION_DIARIA);
		try {
			UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(authentication.getName());
			modelAndView.addObject("unidad", unidad);
			modelAndView.addObject("festival", unidad.getFestival());
			modelAndView.addObject("dias", reporteService.getRecaudacionDiaria(authentication.getName()));
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
		}
		return modelAndView;
	}


		// Casos 27-29: ABM y filtros de empleados propios del responsable.
		@GetMapping("/empleados")
		public ModelAndView empleados(Authentication authentication,
									  @RequestParam(required = false) String nombre,
									  @RequestParam(required = false) String apellido,
									  @RequestParam(required = false) String dni,
									  @RequestParam(required = false) String email,
									  @RequestParam(required = false) String tipo,
									  @RequestParam(required = false) String estado,
									  @RequestParam(required = false) String guardado) {
			ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_LIST);
			try {
				modelAndView.addObject("empleados",
						empleadoService.listarActivos(authentication.getName(), nombre, apellido, dni, email, tipo));
				modelAndView.addObject("todosLosEmpleados", empleadoService.listarTodosDeLaUnidad(authentication.getName()));
				modelAndView.addObject("nombre", nombre);
				modelAndView.addObject("apellido", apellido);
				modelAndView.addObject("dni", dni);
				modelAndView.addObject("email", email);
				modelAndView.addObject("tipo", tipo);
				modelAndView.addObject("estado", estado);
				modelAndView.addObject("guardado", guardado != null);
			} catch (ValidacionException e) {
				modelAndView.addObject("errorGeneral", e.getMessage());
			}
			return modelAndView;
		}

		@GetMapping("/empleados/nuevo")
		public ModelAndView altaEmpleado() {
			return formularioEmpleado(empleadoService.prepararAlta(), false);
		}

		@GetMapping("/empleados/{id}/editar")
		public ModelAndView editarEmpleado(Authentication authentication, @PathVariable Integer id) {
			try {
				return formularioEmpleado(empleadoService.prepararEdicion(authentication.getName(), id), true);
			} catch (ValidacionException e) {
				return errorEmpleados(e);
			}
		}

		@PostMapping("/empleados")
		public ModelAndView crearEmpleado(Authentication authentication,
                                          @Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado, BindingResult bindingResult) {
			if (bindingResult.hasErrors()) {
				return formularioEmpleado(empleado, false);
			}
			try {
				empleadoService.crear(authentication.getName(), empleado);
				return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT + "?guardado");
			} catch (ValidacionException e) {
				if (e.getCampo() == null) return errorEmpleados(e);
				bindingResult.rejectValue(e.getCampo(), "error.empleado", e.getMessage());
				return formularioEmpleado(empleado, false);
			} catch (org.springframework.mail.MailException e) {
				bindingResult.reject("error.email", "No se pudo enviar el correo. Revisá la configuración SMTP.");
				return formularioEmpleado(empleado, false);
			}
		}

		@PostMapping("/empleados/{id}")
		public ModelAndView actualizarEmpleado(Authentication authentication, @PathVariable Integer id,
											   @Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado, BindingResult bindingResult) {
			empleado.setId(id);
			if (bindingResult.hasErrors()) return formularioEmpleado(empleado, true);
			try {
				empleadoService.modificar(authentication.getName(), id, empleado);
				return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT + "?guardado");
			} catch (ValidacionException e) {
				if (e.getCampo() == null) return errorEmpleados(e);
				bindingResult.rejectValue(e.getCampo(), "error.empleado", e.getMessage());
				return formularioEmpleado(empleado, true);
			}
		}

		@PostMapping("/empleados/{id}/estado")
		public ModelAndView cambiarEstadoEmpleado(Authentication authentication, @PathVariable Integer id) {
			try {
				boolean activo = empleadoService.cambiarEstado(authentication.getName(), id);
				return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT
						+ "?estado=" + (activo ? "reactivado" : "baja"));
			} catch (ValidacionException e) {
				return errorEmpleados(e);
			}
		}

		private ModelAndView formularioEmpleado(EmpleadoFormDTO empleado, boolean edicion) {
			ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
			modelAndView.addObject("empleado", empleado);
			modelAndView.addObject("edicion", edicion);
			modelAndView.addObject("categorias", CategoriaCocina.values());
			modelAndView.addObject("turnos", Turno.values());
			return modelAndView;
		}

		private ModelAndView errorEmpleados(ValidacionException e) {
			ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_LIST);
			modelAndView.addObject("errorGeneral", e.getMessage());
			modelAndView.addObject("empleados", java.util.List.of());
			return modelAndView;
		}
	}

