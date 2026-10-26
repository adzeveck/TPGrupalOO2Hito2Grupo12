package com.unla.epicentrogourmet.services.implementation;

import java.util.List;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.entities.Festival;
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

}
