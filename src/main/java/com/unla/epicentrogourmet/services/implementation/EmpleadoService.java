package com.unla.epicentrogourmet.services.implementation;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.EmpleadoFormDTO;
import com.unla.epicentrogourmet.entities.Cajero;
import com.unla.epicentrogourmet.entities.Cocinero;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.entities.User;
import com.unla.epicentrogourmet.entities.UserRole;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.repositories.IUserRepository;
import com.unla.epicentrogourmet.services.IEmailService;
import com.unla.epicentrogourmet.services.IEmpleadoService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;



@Service("empleadoService")
public class EmpleadoService implements IEmpleadoService {

    private static final String ROL_EMPLEADO = "ROLE_EMPLEADO";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IPersonalRepository personalRepository;
    private final IUserRepository userRepository;
    private final IUnidadDeVentaService unidadDeVentaService;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public EmpleadoService(IPersonalRepository personalRepository, IUserRepository userRepository,
                           IUnidadDeVentaService unidadDeVentaService, IEmailService emailService,
                           PasswordEncoder passwordEncoder) {
        this.personalRepository = personalRepository;
        this.userRepository = userRepository;
        this.unidadDeVentaService = unidadDeVentaService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personal> listarActivos(String username, String nombre, String apellido, String dni,
                                        String email, String tipoEmpleado) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        return personalRepository.findByUnidadIdAndUsuarioEnabledTrueOrderByApellidoAscNombreAsc(unidad.getId())
                .stream()
                .filter(p -> contiene(p.getNombre(), nombre))
                .filter(p -> contiene(p.getApellido(), apellido))
                .filter(p -> contiene(p.getDni(), dni))
                .filter(p -> contiene(p.getUsuario().getUsername(), email))
                .filter(p -> tipoEmpleado == null || tipoEmpleado.isBlank()
                        || tipoEmpleado.equals(tipoDe(p)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personal> listarTodosDeLaUnidad(String username) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        return personalRepository.findByUnidadIdOrderByApellidoAscNombreAsc(unidad.getId());
    }

    @Override
    public EmpleadoFormDTO prepararAlta() {
        return new EmpleadoFormDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoFormDTO prepararEdicion(String username, Integer empleadoId) {
        Personal empleado = getEmpleadoDeLaUnidad(username, empleadoId);
        EmpleadoFormDTO dto = new EmpleadoFormDTO();
        dto.setId(empleado.getId());
        dto.setNombre(empleado.getNombre());
        dto.setApellido(empleado.getApellido());
        dto.setDni(empleado.getDni());
        dto.setFechaNacimiento(empleado.getFechaNacimiento());
        dto.setEmail(empleado.getUsuario().getUsername());
        if (empleado instanceof Cocinero cocinero) {
            dto.setTipoEmpleado("COCINERO");
            dto.setCategoria(cocinero.getCategoria());
            dto.setEspecialidad(cocinero.getEspecialidad());
        } else if (empleado instanceof Cajero cajero) {
            dto.setTipoEmpleado("CAJERO");
            dto.setTurno(cajero.getTurno());
        }
        return dto;
    }

    @Override
    @Transactional
    public void crear(String username, EmpleadoFormDTO dto) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        String email = normalizarEmail(dto.getEmail());
        validarEmailDisponible(email, null);
        validarDniDisponible(dto.getDni().trim(), null);

        String claveTemporal = generarClaveTemporal();
        User usuario = new User(email, passwordEncoder.encode(claveTemporal), true);
        usuario.getUserRoles().add(new UserRole(null, usuario, ROL_EMPLEADO));
        Personal empleado = crearTipo(dto);
        completarDatos(empleado, dto, usuario, unidad);

        // El alta solo queda persistida si el correo con las credenciales fue aceptado por SMTP.
        emailService.enviar(email, "Acceso a Epicentro Gourmet",
                "Hola " + dto.getNombre().trim() + ",\n\n"
                        + "Se creó tu usuario para Epicentro Gourmet.\n"
                        + "Usuario: " + email + "\n"
                        + "Contraseña temporal: " + claveTemporal + "\n\n"
                        + "Conservá estos datos para iniciar sesión.");

        // Personal.usuario tiene cascade ALL: guarda usuario, rol y subtipo dentro de la misma transacción.
        personalRepository.save(empleado);
    }

    @Override
    @Transactional
    public void modificar(String username, Integer empleadoId, EmpleadoFormDTO dto) {
        Personal empleado = getEmpleadoDeLaUnidad(username, empleadoId);
        String email = normalizarEmail(dto.getEmail());
        validarEmailDisponible(email, empleado.getUsuario().getId());
        validarDniDisponible(dto.getDni().trim(), empleado.getId());
        if (!tipoDe(empleado).equals(dto.getTipoEmpleado())) {
            throw new ValidacionException("tipoEmpleado", "No se puede cambiar el tipo de un empleado existente");
        }

        String emailActual = empleado.getUsuario().getUsername();
        if (!emailActual.equals(email)) {
            throw new ValidacionException("email", "El correo de acceso no se puede cambiar desde la modificación");
        }
        completarDatos(empleado, dto, empleado.getUsuario(), empleado.getUnidad());
    }

    @Override
    @Transactional
    public boolean cambiarEstado(String username, Integer empleadoId) {
        Personal empleado = getEmpleadoDeLaUnidad(username, empleadoId);
        // false = baja lógica; true = reactivación del mismo usuario y del mismo registro Personal.
        empleado.getUsuario().setEnabled(!empleado.getUsuario().isEnabled());
        return empleado.getUsuario().isEnabled();
    }

    private Personal getEmpleadoDeLaUnidad(String username, Integer empleadoId) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        return personalRepository.findByIdAndUnidadId(empleadoId, unidad.getId())
                .orElseThrow(() -> new ValidacionException(null,
                        "El empleado no pertenece a tu unidad de venta"));
    }

    private Personal crearTipo(EmpleadoFormDTO dto) {
        if ("COCINERO".equals(dto.getTipoEmpleado())) {
            Cocinero cocinero = new Cocinero();
            cocinero.setCategoria(dto.getCategoria());
            cocinero.setEspecialidad(limpiar(dto.getEspecialidad()));
            return cocinero;
        }
        Cajero cajero = new Cajero();
        cajero.setTurno(dto.getTurno());
        return cajero;
    }

    private void completarDatos(Personal empleado, EmpleadoFormDTO dto, User usuario, UnidadDeVenta unidad) {
        empleado.setNombre(dto.getNombre().trim());
        empleado.setApellido(dto.getApellido().trim());
        empleado.setDni(dto.getDni().trim());
        empleado.setFechaNacimiento(dto.getFechaNacimiento());
        if (empleado.getFechaIngreso() == null) {
            empleado.setFechaIngreso(LocalDate.now());
        }
        empleado.setUsuario(usuario);
        empleado.setUnidad(unidad);
        if (empleado instanceof Cocinero cocinero) {
            cocinero.setCategoria(dto.getCategoria());
            cocinero.setEspecialidad(limpiar(dto.getEspecialidad()));
        } else if (empleado instanceof Cajero cajero) {
            cajero.setTurno(dto.getTurno());
        }
    }

    private void validarEmailDisponible(String email, Integer usuarioIdActual) {
        if (userRepository.existsByUsername(email)) {
            boolean esActual = usuarioIdActual != null && userRepository.findByUsernameAndFetchUserRolesEagerly(email)
                    .getId().equals(usuarioIdActual);
            if (!esActual) {
                throw new ValidacionException("email", "Ya existe un usuario con ese correo");
            }
        }
    }
    private void validarDniDisponible(String dni, Integer empleadoIdActual) {
        personalRepository.findByDni(dni).ifPresent(otro -> {
            if (empleadoIdActual == null || !otro.getId().equals(empleadoIdActual)) {
                throw new ValidacionException("dni", "Ya existe un empleado con ese DNI");
            }
        });
    }
    /*private void validarDniDisponible(String dni, Integer empleadoIdActual) {
        var empleadoExistente = personalRepository.findByDni(dni);

        if (empleadoExistente.isPresent()
                && (empleadoIdActual == null
                || !empleadoExistente.get().getId().equals(empleadoIdActual))) {
            throw new ValidacionException("dni", "Ya existe un empleado con ese DNI");
        }
    }*/

    private String generarClaveTemporal() {
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean contiene(String valor, String filtro) {
        return filtro == null || filtro.isBlank()
                || valor.toLowerCase(Locale.ROOT).contains(filtro.trim().toLowerCase(Locale.ROOT));
    }

    private String tipoDe(Personal empleado) {
        return empleado instanceof Cocinero ? "COCINERO" : "CAJERO";
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
