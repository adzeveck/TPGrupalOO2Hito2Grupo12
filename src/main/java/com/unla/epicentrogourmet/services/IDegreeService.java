package com.unla.epicentrogourmet.services;
import java.util.List;

import com.unla.epicentrogourmet.entities.Degree;
import com.unla.epicentrogourmet.dtos.DegreeDTO;


@SuppressWarnings("unused")
public interface IDegreeService {

	List<Degree> getAll();

	void insertOrUpdate(DegreeDTO degreeDTO);

	boolean remove(int id);
}

