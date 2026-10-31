package com.unla.epicentrogourmet.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.unla.epicentrogourmet.dtos.EmpleadoFormDTO;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.helpers.ViewRouteHelper;
import com.unla.epicentrogourmet.services.IEmpleadoService;
import com.unla.epicentrogourmet.services.IPedidoService;

import jakarta.validation.Valid;

@Controller
public class EmpleadoController {
	private static final Logger log = LoggerFactory.getLogger(EmpleadoController.class);

	private final IPedidoService pedidoService;
	private final IEmpleadoService empleadoService;

	public EmpleadoController(IPedidoService pedidoService, IEmpleadoService empleadoService) {
		this.pedidoService = pedidoService;
		this.empleadoService = empleadoService;
	}

	// Panel del empleado que inicia sesión con ROLE_EMPLEADO.
	@GetMapping("/empleado/index")
	@PreAuthorize("hasRole('EMPLEADO')")
	public ModelAndView index(Authentication authentication) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_INDEX);
		modelAndView.addObject(
				"puedeCargarPedidos",
				pedidoService.puedeCargarPedidos(authentication.getName()));
		return modelAndView;
	}

	// Listado, filtros y acciones del personal de la unidad del responsable.
	@GetMapping("/responsable/empleados")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView listar(Authentication authentication,
							   @RequestParam(required = false) String nombre,
							   @RequestParam(required = false) String apellido,
							   @RequestParam(required = false) String dni,
							   @RequestParam(required = false) String email,
							   @RequestParam(required = false) String tipo,
							   @RequestParam(required = false) String estado,
							   @RequestParam(required = false) String guardado) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_LIST);
		try {
			String username = authentication.getName();
			modelAndView.addObject(
					"empleados",
					empleadoService.listarActivos(username, nombre, apellido, dni, email, tipo));
			modelAndView.addObject("todosLosEmpleados", empleadoService.listarTodosDeLaUnidad(username));
			modelAndView.addObject("nombre", nombre);
			modelAndView.addObject("apellido", apellido);
			modelAndView.addObject("dni", dni);
			modelAndView.addObject("email", email);
			modelAndView.addObject("tipo", tipo);
			modelAndView.addObject("estado", estado);
			modelAndView.addObject("guardado", guardado != null);
		} catch (ValidacionException e) {
			modelAndView.addObject("errorGeneral", e.getMessage());
			modelAndView.addObject("empleados", java.util.List.of());
			modelAndView.addObject("todosLosEmpleados", java.util.List.of());
		}
		return modelAndView;
	}

	// GET /responsable/empleados/nuevo
	@GetMapping("/responsable/empleados/nuevo")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView formAlta() {
		return formConDatos(empleadoService.prepararAlta(), false);
	}

	// GET /responsable/empleados/{id}/editar
	@GetMapping("/responsable/empleados/{id}/editar")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView formEdicion(Authentication authentication, @PathVariable Integer id) {
		try {
			return formConDatos(
					empleadoService.prepararEdicion(authentication.getName(), id), true);
		} catch (ValidacionException e) {
			return vistaError(e);
		}
	}

	// POST /responsable/empleados
	@PostMapping("/responsable/empleados")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView crear(Authentication authentication,
							  @Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado,
							  BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return formConDatos(empleado, false);
		}
		try {
			empleadoService.crear(authentication.getName(), empleado);
			return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT + "?guardado");
		} catch (ValidacionException e) {
			if (e.getCampo() == null) {
				return vistaError(e);
			}
			bindingResult.rejectValue(e.getCampo(), "error.empleado", e.getMessage());
			return formConDatos(empleado, false);
		} catch (org.springframework.mail.MailException e) {
			log.error("Error al enviar las credenciales del empleado", e);
			bindingResult.reject(
					"error.email",
					"No se pudo enviar el correo. Revisá la configuración SMTP.");
			return formConDatos(empleado, false);
		}
	}

	// POST /responsable/empleados/{id}
	@PostMapping("/responsable/empleados/{id}")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView modificar(Authentication authentication,
								  @PathVariable Integer id,
								  @Valid @ModelAttribute("empleado") EmpleadoFormDTO empleado,
								  BindingResult bindingResult) {
		empleado.setId(id);
		if (bindingResult.hasErrors()) {
			return formConDatos(empleado, true);
		}
		try {
			empleadoService.modificar(authentication.getName(), id, empleado);
			return new ModelAndView("redirect:" + ViewRouteHelper.EMPLEADOS_ROOT + "?guardado");
		} catch (ValidacionException e) {
			if (e.getCampo() == null) {
				return vistaError(e);
			}
			bindingResult.rejectValue(e.getCampo(), "error.empleado", e.getMessage());
			return formConDatos(empleado, true);
		}
	}

	// POST /responsable/empleados/{id}/estado: baja lógica o reactivación.
	@PostMapping("/responsable/empleados/{id}/estado")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView cambiarEstado(Authentication authentication, @PathVariable Integer id) {
		try {
			boolean activo = empleadoService.cambiarEstado(authentication.getName(), id);
			return new ModelAndView(
					"redirect:" + ViewRouteHelper.EMPLEADOS_ROOT
							+ "?estado=" + (activo ? "reactivado" : "baja"));
		} catch (ValidacionException e) {
			return vistaError(e);
		}
	}

	private ModelAndView formConDatos(EmpleadoFormDTO empleado, boolean edicion) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_FORM);
		modelAndView.addObject("empleado", empleado);
		modelAndView.addObject("edicion", edicion);
		modelAndView.addObject("categorias", com.unla.epicentrogourmet.enums.CategoriaCocina.values());
		modelAndView.addObject("turnos", com.unla.epicentrogourmet.enums.Turno.values());
		return modelAndView;
	}

	private ModelAndView vistaError(ValidacionException e) {
		ModelAndView modelAndView = new ModelAndView(ViewRouteHelper.EMPLEADO_LIST);
		modelAndView.addObject("errorGeneral", e.getMessage());
		modelAndView.addObject("empleados", java.util.List.of());
		modelAndView.addObject("todosLosEmpleados", java.util.List.of());
		return modelAndView;
	}

	// POST /responsable/empleados/{id}/restablecer-clave
	@PostMapping("/responsable/empleados/{id}/restablecer-clave")
	@PreAuthorize("hasRole('RESPONSABLE')")
	public ModelAndView restablecerClave(
			Authentication authentication,
			@PathVariable Integer id) {

		try {
			empleadoService.restablecerClave(
					authentication.getName(),
					id);

			return new ModelAndView(
					"redirect:"
							+ ViewRouteHelper.EMPLEADOS_ROOT
							+ "?estado=enviadas");

		} catch (ValidacionException e) {

			return vistaError(e);

		} catch (MailException e) {

			log.error(
					"No se pudo restablecer la contraseña "
							+ "y enviar las credenciales del empleado {}",
					id,
					e);

			return new ModelAndView(
					"redirect:"
							+ ViewRouteHelper.EMPLEADOS_ROOT
							+ "?estado=errorCorreo");
		}
	}
}
