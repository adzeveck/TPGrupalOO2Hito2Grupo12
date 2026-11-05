package com.unla.epicentrogourmet.services.implementation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.LiquidacionHaberes;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.ILiquidacionHaberesRepository;
import com.unla.epicentrogourmet.services.ILiquidacionCerradaService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("liquidacionCerradaService")
public class LiquidacionCerradaService implements ILiquidacionCerradaService {

    private final ILiquidacionHaberesRepository liquidacionHaberesRepository;
    private final IUnidadDeVentaService unidadDeVentaService;

    public LiquidacionCerradaService(ILiquidacionHaberesRepository liquidacionHaberesRepository,
                                     IUnidadDeVentaService unidadDeVentaService) {
        this.liquidacionHaberesRepository = liquidacionHaberesRepository;
        this.unidadDeVentaService = unidadDeVentaService;
    }

    // readOnly: solo lee. La sesión queda abierta durante la operación, así que el empleado, la unidad
    // y el festival (LAZY) de cada liquidación se cargan bien al armar el DTO
    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionHaberesDTO> listarDelEmpleado(String username) {
        return liquidacionHaberesRepository.findByEmpleadoUsuarioUsernameOrderByHastaDesc(username).stream()
                .map(this::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionHaberesDTO obtenerDelEmpleado(String username, Integer liquidacionId) {
        // Se busca por id Y por el usuario logueado: no se puede pedir la liquidación de otro empleado
        LiquidacionHaberes liquidacion = liquidacionHaberesRepository
                .findByIdAndEmpleadoUsuarioUsername(liquidacionId, username)
                .orElseThrow(() -> new ValidacionException(null, "La liquidación no existe o no es tuya"));
        return aDTO(liquidacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiquidacionHaberesDTO> listarDeLaUnidad(String username) {
        // La unidad sale del responsable logueado: no puede ver las de otra unidad
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        return liquidacionHaberesRepository.findByUnidadIdOrderByHastaDescEmpleadoApellidoAsc(unidad.getId())
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LiquidacionHaberesDTO obtenerDeLaUnidad(String username, Integer liquidacionId) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        LiquidacionHaberes liquidacion = liquidacionHaberesRepository
                .findByIdAndUnidadId(liquidacionId, unidad.getId())
                .orElseThrow(() -> new ValidacionException(null, "La liquidación no pertenece a tu unidad"));
        return aDTO(liquidacion);
    }

    // Lo guardado al cerrar, listo para mostrar y para imprimir en el recibo: no se recalcula nada
    private LiquidacionHaberesDTO aDTO(LiquidacionHaberes liquidacion) {
        Personal empleado = liquidacion.getEmpleado();
        LiquidacionHaberesDTO dto = new LiquidacionHaberesDTO();
        dto.setLiquidacionId(liquidacion.getId());
        dto.setFechaCierre(liquidacion.getFecha());
        dto.setEmpleadoId(empleado.getId());
        dto.setEmpleado(empleado.getNombre() + " " + empleado.getApellido());
        dto.setDni(empleado.getDni());
        dto.setPuesto(liquidacion.getPuesto());
        dto.setUnidad(liquidacion.getUnidad().getNombreComercial());
        dto.setFestival(liquidacion.getUnidad().getFestival().getNombre());
        dto.setFechaInicio(liquidacion.getDesde());
        dto.setFechaFin(liquidacion.getHasta());
        dto.setDias(liquidacion.getDias());
        dto.setSueldoBase(liquidacion.getSueldoBase());
        // Las cerradas antes de guardar el concepto no lo tienen: se muestra un texto genérico
        dto.setConceptoAdicional(liquidacion.getConceptoAdicional() != null
                ? liquidacion.getConceptoAdicional() : "Adicional");
        dto.setAdicional(liquidacion.getAdicional());
        dto.setSubtotal(liquidacion.calcularSubtotal());
        dto.setPorcentaje(liquidacion.getPorcentaje());
        dto.setTotal(liquidacion.getTotal());
        return dto;
    }
}