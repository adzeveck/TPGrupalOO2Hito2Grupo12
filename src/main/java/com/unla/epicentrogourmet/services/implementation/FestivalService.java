package com.unla.epicentrogourmet.services.implementation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.FestivalConUnidadesDTO;
import com.unla.epicentrogourmet.dtos.UnidadConCanonDTO;
import com.unla.epicentrogourmet.entities.Costo;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.enums.EstadoFestival;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IFestivalRepository;
import com.unla.epicentrogourmet.services.IFestivalService;

@Service("festivalService")
public class FestivalService implements IFestivalService {

	private final IFestivalRepository festivalRepository;

	public FestivalService(IFestivalRepository festivalRepository) {
		this.festivalRepository = festivalRepository;
	}

	@Override
	public List<Festival> getAllWithUnidades() {
		return festivalRepository.findAllWithUnidades();
	}

	@Override
	public List<Festival> getVigentes() {
		return festivalRepository.findByFechaFinGreaterThanEqualOrderByFechaInicioAsc(LocalDate.now());
	}

	@Override
	public Festival getConUnidades(Integer id) {
		return festivalRepository.findByIdWithUnidades(id)
				.orElseThrow(() -> new ValidacionException("festivalId", "El festival elegido no existe"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<FestivalConUnidadesDTO> getFestivalesConUnidades(LocalDate hoy) {
		// Una sola consulta trae los festivales (ordenados por fecha de inicio) con sus unidades
		return festivalRepository.findAllWithUnidades().stream()
				.map(festival -> aFestivalConUnidades(festival, hoy))
				.toList();
	}

	private FestivalConUnidadesDTO aFestivalConUnidades(Festival festival, LocalDate hoy) {
		FestivalConUnidadesDTO dto = new FestivalConUnidadesDTO();
		dto.setId(festival.getId());
		dto.setNombre(festival.getNombre());
		dto.setTemporada(festival.getTemporada());
		dto.setFechaInicio(festival.getFechaInicio());
		dto.setFechaFin(festival.getFechaFin());
		dto.setEstado(EstadoFestival.de(festival.getFechaInicio(), festival.getFechaFin(), hoy));

		// El canon de cada unidad se calcula con los montos de SU festival (polimorfismo de calcularCanon)
		Costo costo = festival.getCosto();
		List<UnidadConCanonDTO> unidades = festival.getUnidades().stream()
				.map(unidad -> aUnidadConCanon(unidad, costo))
				.sorted(Comparator.comparing(UnidadConCanonDTO::getNombreComercial))
				.toList();
		dto.setUnidades(unidades);
		dto.setTotalCanon(unidades.stream()
				.map(UnidadConCanonDTO::getCanon)
				.reduce(BigDecimal.ZERO, BigDecimal::add));
		return dto;
	}

	private UnidadConCanonDTO aUnidadConCanon(UnidadDeVenta unidad, Costo costo) {
		UnidadConCanonDTO dto = new UnidadConCanonDTO();
		dto.setCodigo(unidad.getCodigo());
		dto.setNombreComercial(unidad.getNombreComercial());
		dto.setTipo(unidad.getTipo());
		dto.setSuperficie(unidad.getSuperficie());
		dto.setResponsable(unidad.getResponsable().getUsername());
		dto.setCanon(unidad.calcularCanon(costo));
		return dto;
	}
}
