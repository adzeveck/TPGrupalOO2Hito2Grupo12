package com.unla.epicentrogourmet.services.implementation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.PuestoEmpleadoDTO;
import com.unla.epicentrogourmet.entities.Cajero;
import com.unla.epicentrogourmet.entities.Cocinero;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.services.IPuestoEmpleadoService;

@Service("puestoEmpleadoService")
public class PuestoEmpleadoService implements IPuestoEmpleadoService {

    private final IPersonalRepository personalRepository;

    public PuestoEmpleadoService(IPersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    // readOnly: solo lee. La sesión queda abierta durante el método, así que unidad y festival (LAZY) se cargan bien
    @Override
    @Transactional(readOnly = true)
    public PuestoEmpleadoDTO obtener(String username) {
        Personal empleado = personalRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new ValidacionException(null, "Tu usuario no corresponde a un empleado"));
        UnidadDeVenta unidad = empleado.getUnidad();
        if (unidad == null) {
            throw new ValidacionException(null, "No estás asignado a ninguna unidad de venta");
        }
        Festival festival = unidad.getFestival();
        LocalDate hoy = LocalDate.now();

        PuestoEmpleadoDTO dto = new PuestoEmpleadoDTO();
        dto.setEmpleado(empleado.getNombre() + " " + empleado.getApellido());
        dto.setDni(empleado.getDni());
        dto.setPuesto(puestoDe(empleado));
        dto.setFechaIngreso(empleado.getFechaIngreso());
        dto.setUnidad(unidad.getNombreComercial());
        dto.setCodigoUnidad(unidad.getCodigo());
        dto.setTipoUnidad(unidad.getTipo()); // polimorfismo: cada subclase informa el suyo
        dto.setSuperficie(unidad.getSuperficie());
        dto.setFestival(festival.getNombre());
        dto.setTemporada(festival.getTemporada());
        dto.setFechaInicio(festival.getFechaInicio());
        dto.setFechaFin(festival.getFechaFin());
        dto.setDias(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);
        dto.setEstado(calcularEstado(hoy, festival.getFechaInicio(), festival.getFechaFin()));
        dto.setEnCurso(!hoy.isBefore(festival.getFechaInicio()) && !hoy.isAfter(festival.getFechaFin()));
        return dto;
    }

    // El tipo de empleado se sabe por la subclase (instanceof), no por un atributo
    private String puestoDe(Personal empleado) {
        if (empleado instanceof Cocinero cocinero) {
            return switch (cocinero.getCategoria()) {
                case COCINERO -> "Cocinero";
                case AYUDANTE -> "Ayudante de cocina";
                case LAVAPLATOS -> "Lavaplatos";
            };
        }
        if (empleado instanceof Cajero cajero) {
            return "Cajero (turno " + (cajero.getTurno().name().equals("NOCHE") ? "noche" : "mañana") + ")";
        }
        return "Empleado";
    }

    // Antes del inicio: por comenzar. Después del fin: finalizado. Entre medio (inclusive): en curso
    static String calcularEstado(LocalDate hoy, LocalDate inicio, LocalDate fin) {
        if (hoy.isBefore(inicio)) {
            return "Por comenzar";
        }
        if (hoy.isAfter(fin)) {
            return "Finalizado";
        }
        return "En curso";
    }
}