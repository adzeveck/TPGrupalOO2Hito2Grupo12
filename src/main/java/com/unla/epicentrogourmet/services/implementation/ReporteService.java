package com.unla.epicentrogourmet.services.implementation;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.PlatoDTO;
import com.unla.epicentrogourmet.dtos.ReporteMayoresCanonDTO;
import com.unla.epicentrogourmet.entities.Costo;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPlatoRepository;
import com.unla.epicentrogourmet.services.IFestivalService;
import com.unla.epicentrogourmet.services.IReporteService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("reporteService")
public class ReporteService implements IReporteService {

	private static final int LIMITE_POR_DEFECTO = 5;

	private final IFestivalService festivalService;
	private final IUnidadDeVentaService unidadDeVentaService;
	private final IPlatoRepository platoRepository;

	public ReporteService(IFestivalService festivalService, IUnidadDeVentaService unidadDeVentaService,
			IPlatoRepository platoRepository) {
		this.festivalService = festivalService;
		this.unidadDeVentaService = unidadDeVentaService;
		this.platoRepository = platoRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReporteMayoresCanonDTO> getMayoresCanon(Integer festivalId, Integer limite) {
		int cantidad = limite == null ? LIMITE_POR_DEFECTO : limite;
		if (cantidad < 1 || cantidad > 100) {
			throw new ValidacionException("limite", "La cantidad de unidades tiene que estar entre 1 y 100");
		}

		Festival festival = festivalService.getConUnidades(festivalId);
		Costo costo = festival.getCosto(); // el canon se calcula con los montos del festival, nada hardcodeado
		return festival.getUnidades().stream()
				.map(unidad -> aReporte(unidad, costo))
				.sorted(Comparator.comparing(ReporteMayoresCanonDTO::getCanon).reversed()
						.thenComparing(ReporteMayoresCanonDTO::getNombreComercial))
				.limit(cantidad)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PlatoDTO getPlatoEstrella(String username) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		List<PlatoDTO> ranking = platoRepository.findRankingDePlatos(unidad.getId());
		if (ranking.isEmpty()) {
			throw new ValidacionException(null, "Tu unidad todavía no tiene pedidos cargados");
		}
		PlatoDTO estrella = ranking.get(0);
		estrella.setFestival(unidad.getFestival().getNombre());
		estrella.setUnidad(unidad.getNombreComercial());
		return estrella;
	}

	private ReporteMayoresCanonDTO aReporte(UnidadDeVenta unidad, Costo costo) {
		ReporteMayoresCanonDTO dto = new ReporteMayoresCanonDTO();
		dto.setNombreComercial(unidad.getNombreComercial());
		dto.setCodigo(unidad.getCodigo());
		dto.setTipo(unidad.getTipo());
		dto.setCanon(unidad.calcularCanon(costo));
		return dto;
	}
}
