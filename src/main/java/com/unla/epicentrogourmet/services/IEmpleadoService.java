package com.unla.epicentrogourmet.services;

import java.util.List;

import com.unla.epicentrogourmet.dtos.EmpleadoFormDTO;
import com.unla.epicentrogourmet.entities.Personal;

public interface IEmpleadoService {

    List<Personal> listarActivos(String username, String nombre, String apellido, String dni,
                                 String email, String tipoEmpleado);

    List<Personal> listarTodosDeLaUnidad(String username);

    EmpleadoFormDTO prepararAlta();

    EmpleadoFormDTO prepararEdicion(String username, Integer empleadoId);

    void crear(String username, EmpleadoFormDTO dto);

    void modificar(String username, Integer empleadoId, EmpleadoFormDTO dto);

    boolean cambiarEstado(String username, Integer empleadoId);

    void restablecerClave(String username, Integer empleadoId);
}
