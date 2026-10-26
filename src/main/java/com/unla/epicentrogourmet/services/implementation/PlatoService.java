package com.unla.epicentrogourmet.services.implementation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.PlatoFormDTO;
import com.unla.epicentrogourmet.entities.Plato;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IPlatoRepository;
import com.unla.epicentrogourmet.services.IPlatoService;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("platoService")
public class PlatoService implements IPlatoService {

	private final IPlatoRepository platoRepository;
	private final IUnidadDeVentaService unidadDeVentaService;

	public PlatoService(IPlatoRepository platoRepository, IUnidadDeVentaService unidadDeVentaService) {
		this.platoRepository = platoRepository;
		this.unidadDeVentaService = unidadDeVentaService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Plato> listar(String username) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		return platoRepository.findByUnidadIdOrderByNombreAsc(unidad.getId());
	}

	@Override
	public PlatoFormDTO prepararAlta() {
		return new PlatoFormDTO();
	}

	@Override
	@Transactional(readOnly = true)
	public PlatoFormDTO prepararEdicion(String username, Integer platoId) {
		Plato plato = getPlatoDeLaUnidad(username, platoId);
		PlatoFormDTO dto = new PlatoFormDTO();
		dto.setId(plato.getId());
		dto.setNombre(plato.getNombre());
		dto.setPrecio(plato.getPrecio());
		dto.setCosto(plato.getCosto());
		return dto;
	}

	@Override
	@Transactional
	public void crear(String username, PlatoFormDTO dto) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		String nombre = dto.getNombre().trim();
		if (platoRepository.existsByUnidadIdAndNombreIgnoreCase(unidad.getId(), nombre)) {
			throw new ValidacionException("nombre", "Ya tenés un plato con ese nombre en tu unidad");
		}

		Plato plato = new Plato();
		plato.setNombre(nombre);
		plato.setPrecio(dto.getPrecio());
		plato.setCosto(dto.getCosto());
		plato.setUnidad(unidad); // el plato es el dueño de la FK unidad_id
		platoRepository.save(plato);
	}

	@Override
	@Transactional
	public void modificar(String username, Integer platoId, PlatoFormDTO dto) {
		Plato plato = getPlatoDeLaUnidad(username, platoId);
		String nombre = dto.getNombre().trim();
		if (platoRepository.existsByUnidadIdAndNombreIgnoreCaseAndIdNot(plato.getUnidad().getId(), nombre, platoId)) {
			throw new ValidacionException("nombre", "Ya tenés un plato con ese nombre en tu unidad");
		}

		plato.setNombre(nombre);
		plato.setPrecio(dto.getPrecio());
		plato.setCosto(dto.getCosto());
	}

	@Override
	@Transactional
	public boolean cambiarEstado(String username, Integer platoId) {
		Plato plato = getPlatoDeLaUnidad(username, platoId);
		plato.setActivo(!plato.isActivo());
		return plato.isActivo();
	}

	// Buscar por id y unidad a la vez: un responsable no puede tocar el menú de otra unidad
	private Plato getPlatoDeLaUnidad(String username, Integer platoId) {
		UnidadDeVenta unidad = unidadDeVentaService.getDelResponsable(username);
		return platoRepository.findByIdAndUnidadId(platoId, unidad.getId())
				.orElseThrow(() -> new ValidacionException(null, "El plato no pertenece a tu unidad de venta"));
	}
}
