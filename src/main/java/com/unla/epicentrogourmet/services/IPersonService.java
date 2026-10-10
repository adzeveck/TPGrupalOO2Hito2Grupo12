package com.unla.epicentrogourmet.services;
import java.util.List;
import java.util.Optional;

import com.unla.epicentrogourmet.entities.Person;
import com.unla.epicentrogourmet.dtos.PersonDTO;

public interface IPersonService {

	List<Person> getAll();

	Optional<Person> findById(int id);

	Person findByName(String name);

	Person insertOrUpdate(Person person);

	boolean remove(int id);

	List<PersonDTO> findByDegreeName(String degreeName);
}

