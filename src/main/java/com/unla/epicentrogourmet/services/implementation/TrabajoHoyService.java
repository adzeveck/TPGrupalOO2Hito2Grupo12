package com.unla.epicentrogourmet.services.implementation;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.dtos.PuestoEmpleadoDTO;
import com.unla.epicentrogourmet.dtos.TrabajoHoyDTO;
import com.unla.epicentrogourmet.services.IPuestoEmpleadoService;
import com.unla.epicentrogourmet.services.ITrabajoHoyService;

@Service("trabajoHoyService")
public class TrabajoHoyService implements ITrabajoHoyService {

    private final IPuestoEmpleadoService puestoEmpleadoService;

    public TrabajoHoyService(IPuestoEmpleadoService puestoEmpleadoService) {
        this.puestoEmpleadoService = puestoEmpleadoService;
    }

    // Reutiliza los datos del puesto (festival, unidad, fechas) y los compara con la fecha de hoy.
    // Las validaciones (es empleado, tiene unidad) las hace PuestoEmpleadoService
    @Override
    public TrabajoHoyDTO consultar(String username) {
        PuestoEmpleadoDTO puesto = puestoEmpleadoService.obtener(username);
        LocalDate hoy = LocalDate.now();

        TrabajoHoyDTO dto = new TrabajoHoyDTO();
        dto.setHoy(hoy);
        dto.setTrabajaHoy(!hoy.isBefore(puesto.getFechaInicio()) && !hoy.isAfter(puesto.getFechaFin()));
        dto.setMensaje(armarMensaje(hoy, puesto.getFechaInicio(), puesto.getFechaFin()));
        dto.setPuesto(puesto);
        return dto;
    }

    // Antes del inicio: cuántos días faltan. Después del fin: hace cuánto terminó.
    // Dentro del período (inclusive): trabaja, y cuántos días le quedan
    static String armarMensaje(LocalDate hoy, LocalDate inicio, LocalDate fin) {
        if (hoy.isBefore(inicio)) {
            long dias = ChronoUnit.DAYS.between(hoy, inicio);
            return "Todavía no: el festival empieza " + (dias == 1 ? "mañana" : "en " + dias + " días");
        }
        if (hoy.isAfter(fin)) {
            long dias = ChronoUnit.DAYS.between(fin, hoy);
            return "No: el festival terminó " + (dias == 1 ? "ayer" : "hace " + dias + " días");
        }
        long restan = ChronoUnit.DAYS.between(hoy, fin);
        if (restan == 0) {
            return "Sí, hoy trabajás: es el último día del festival";
        }
        if (restan == 1) {
            return "Sí, hoy trabajás. Mañana es el último día del festival";
        }
        return "Sí, hoy trabajás. Al festival le quedan " + restan + " días";
    }
}
