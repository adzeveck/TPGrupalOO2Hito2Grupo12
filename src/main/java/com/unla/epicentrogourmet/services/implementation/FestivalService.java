package com.unla.epicentrogourmet.services.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.unla.epicentrogourmet.entities.Festival;
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
}
