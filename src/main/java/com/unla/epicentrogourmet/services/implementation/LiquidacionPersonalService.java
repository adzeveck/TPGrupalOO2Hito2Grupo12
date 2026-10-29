package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.LiquidacionHaberesDTO;
import com.unla.epicentrogourmet.dtos.LiquidacionPersonalDTO;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.Personal;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.repositories.IPersonalRepository;
import com.unla.epicentrogourmet.services.ILiquidacionPersonalService;
import com.unla.epicentrogourmet.services.ILiquidacionService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("liquidacionPersonalService")
public class LiquidacionPersonalService implements ILiquidacionPersonalService {

    private final IUnidadDeVentaService unidadDeVentaService;
    private final IPersonalRepository personalRepository;
    private final ILiquidacionService liquidacionService;

    public LiquidacionPersonalService(IUnidadDeVentaService unidadDeVentaService,
                                      IPersonalRepository personalRepository, ILiquidacionService liquidacionService) {
        this.unidadDeVentaService = unidadDeVentaService;
        this.personalRepository = personalRepository;
        this.liquidacionService = liquidacionService;
    }

    // readOnly: solo lee. La sesión queda abierta durante el método, así que la unidad y el festival (LAZY)
    // de cada empleado se cargan bien al liquidarlo
    @Override
    @Transactional(readOnly = true)
    public LiquidacionPersonalDTO calcular(String username) {
        // La unidad sale del responsable logueado: no puede liquidar el personal de otra unidad
        UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
        Festival festival = unidad.getFestival();

        LiquidacionPersonalDTO dto = new LiquidacionPersonalDTO();
        dto.setUnidad(unidad.getNombreComercial());
        dto.setFestival(festival.getNombre());
        dto.setFechaInicio(festival.getFechaInicio());
        dto.setFechaFin(festival.getFechaFin());
        dto.setDias(ChronoUnit.DAYS.between(festival.getFechaInicio(), festival.getFechaFin()) + 1);

        // Los dados de baja (usuario.enabled = false) no cobran: solo el personal activo
        BigDecimal total = BigDecimal.ZERO;
        for (Personal empleado : personalRepository
                .findByUnidadIdAndUsuarioEnabledTrueOrderByApellidoAscNombreAsc(unidad.getId())) {
            // La misma cuenta que ve cada empleado en /empleado/sueldo
            LiquidacionHaberesDTO liquidacion = liquidacionService.calcular(empleado);
            dto.getLiquidaciones().add(liquidacion);
            total = total.add(liquidacion.getTotal());
        }
        dto.setTotalPlanilla(total);
        return dto;
    }
}
