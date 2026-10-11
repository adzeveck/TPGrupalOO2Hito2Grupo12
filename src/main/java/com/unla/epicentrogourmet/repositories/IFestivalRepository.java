package com.unla.epicentrogourmet.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unla.epicentrogourmet.entities.Festival;

@Repository("festivalRepository")
public interface IFestivalRepository extends JpaRepository<Festival, Integer> {
}
