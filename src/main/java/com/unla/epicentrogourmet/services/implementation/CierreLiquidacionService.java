package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.CierreLiquidacionDTO;
import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.LiquidacionHaberes;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.enums.PeriodoLiquidacion;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.ILiquidacionHaberesRepository;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.services.ICierreLiquidacionService;
import com.unla.epicentrogourmet.services.ILiquidacionService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("cierreLiquidacionService")
public class CierreLiquidacionService implements ICierreLiquidacionService {

    private final IUnidadDeVentaService unidadDeVentaService;
    private final IPersonalRepository personalRepository;
    private final ILiquidacionService liquidacionService;
    private final ILiquidacionHaberesRepository liquidacionHaberesRepository;

    public CierreLiquidacionService(IUnidadDeVentaService unidadDeVentaService,
                                    IPersonalRepository personalRepository, ILiquidacionService liquidacionService,
                                    ILiquidacionHaberesRepository liquidacionHaberesRepository) {
        this.unidadDeVentaService = unidadDeVentaService;
        this.personalRepository = personalRepository;
        this.liquidacionService = liquidacionService;
        this.liquidacionHaberesRepository = liquidacionHaberesRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CierreLiquidacionDTO previsualizar(String username, PeriodoLiquidacion periodo) {
        // La unidad sale del responsable logueado: no puede liquidar el personal de otra unidad
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        CierreLiquidacionDTO cierre = new CierreLiquidacionDTO();
        cierre.setUnidad(unidad.getNombreComercial());
        cierre.setFestival(unidad.getFestival().getNombre());

        BigDecimal total = BigDecimal.ZERO;
        for (Personal empleado : empleadosActivos(unidad)) {
            LiquidacionHaberesDTO liquidacion = liquidarPendiente(empleado, periodo);
            if (liquidacion == null) {
                cierre.getSinDiasPorLiquidar().add(empleado.getNombre() + " " + empleado.getApellido());
            } else {
                cierre.getLiquidaciones().add(liquidacion);
                total = total.add(liquidacion.getTotal());
            }
        }
        cierre.setTotal(total);
        return cierre;
    }

    // Todo se recalcula acá, con los datos de la base: el formulario solo manda el período elegido
    @Override
    @Transactional
    public int cerrar(String username, PeriodoLiquidacion periodo) {
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        int guardadas = 0;
        for (Personal empleado : empleadosActivos(unidad)) {
            LiquidacionHaberesDTO liquidacion = liquidarPendiente(empleado, periodo);
            if (liquidacion != null) {
                liquidacionHaberesRepository.save(aEntidad(liquidacion, empleado, unidad));
                guardadas++;
            }
        }
        // Si no hay nada pendiente (por ejemplo, un segundo clic en Confirmar) no se guarda nada
        if (guardadas == 0) {
            throw new ValidacionException(null, "No hay días por liquidar para el período elegido");
        }
        return guardadas;
    }

    // Los dados de baja (usuario.enabled = false) no cobran: solo el personal activo
    private List<Personal> empleadosActivos(UnidadDeVenta unidad) {
        return personalRepository.findByUnidadIdAndUsuarioEnabledTrueOrderByApellidoAscNombreAsc(unidad.getId());
    }

    // El tramo pendiente del empleado: arranca al día siguiente del último liquidado, y llega hasta donde
    // indica el período. Devuelve null si no hay días por liquidar (ya liquidado, o el festival no empezó)
    private LiquidacionHaberesDTO liquidarPendiente(Personal empleado, PeriodoLiquidacion periodo) {
        Festival festival = empleado.getUnidad().getFestival();
        LocalDate ultimoHasta = liquidacionHaberesRepository.findFirstByEmpleadoIdOrderByHastaDesc(empleado.getId())
                .map(LiquidacionHaberes::getHasta)
                .orElse(null);
        LocalDate desde = empleado.calcularDesde(festival, ultimoHasta);
        LocalDate hasta = periodo.calcularHasta(desde, LocalDate.now(), festival.getFechaFin());
        if (desde.isAfter(hasta)) {
            return null;
        }
        return liquidacionService.calcular(empleado, desde, hasta);
    }

    private LiquidacionHaberes aEntidad(LiquidacionHaberesDTO dto, Personal empleado, UnidadDeVenta unidad) {
        LiquidacionHaberes liquidacion = new LiquidacionHaberes();
        liquidacion.setEmpleado(empleado);
        liquidacion.setUnidad(unidad);
        liquidacion.setFecha(LocalDate.now());
        liquidacion.setDesde(dto.getFechaInicio());
        liquidacion.setHasta(dto.getFechaFin());
        liquidacion.setDias((int) dto.getDias());
        liquidacion.setPuesto(dto.getPuesto());
        liquidacion.setSueldoBase(dto.getSueldoBase());
        liquidacion.setAdicional(dto.getAdicional());
        liquidacion.setPorcentaje(dto.getPorcentaje());
        liquidacion.setTotal(dto.getTotal());
        liquidacion.setConceptoAdicional(dto.getConceptoAdicional());
        return liquidacion;
    }
}