package com.unla.epicentrogourmet.services.implementation;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.RegistroUnidadDTO;
import com.unla.epicentrogourmet.entities.Festival;
import com.unla.epicentrogourmet.entities.FoodTruck;
import com.unla.epicentrogourmet.entities.PuestoDesarmable;
import com.unla.epicentrogourmet.entities.UnidadDeVenta;
import com.unla.epicentrogourmet.entities.User;
import com.unla.epicentrogourmet.entities.UserRole;
import com.unla.epicentrogourmet.enums.TipoUnidad;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IFestivalRepository;
import com.unla.epicentrogourmet.repositories.IUnidadDeVentaRepository;
import com.unla.epicentrogourmet.repositories.IUserRepository;
import com.unla.epicentrogourmet.services.IUnidadDeVentaService;

@Service("unidadDeVentaService")
public class UnidadDeVentaService implements IUnidadDeVentaService {

    private static final String ROL_RESPONSABLE = "ROLE_RESPONSABLE";

    private final IUnidadDeVentaRepository unidadDeVentaRepository;
    private final IUserRepository userRepository;
    private final IFestivalRepository festivalRepository;
    private final PasswordEncoder passwordEncoder;

    public UnidadDeVentaService(IUnidadDeVentaRepository unidadDeVentaRepository, IUserRepository userRepository,
                                IFestivalRepository festivalRepository, PasswordEncoder passwordEncoder) {
        this.unidadDeVentaRepository = unidadDeVentaRepository;
        this.userRepository = userRepository;
        this.festivalRepository = festivalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Todo o nada: si algo falla, no queda ni el usuario ni la unidad a medio guardar
    @Override
    @Transactional
    public UnidadDeVenta registrar(RegistroUnidadDTO dto) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        String username = dto.getUsername().trim().toLowerCase();

        Festival festival = festivalRepository.findById(dto.getFestivalId())
                .orElseThrow(() -> new ValidacionException("festivalId", "El festival elegido no existe"));
        if (festival.getFechaFin().isBefore(LocalDate.now())) {
            throw new ValidacionException("festivalId", "El festival elegido ya terminó");
        }

        UnidadDeVenta unidad = crearUnidad(dto);
        unidad.setCodigo(codigo);
        if (!unidad.tieneCodigoValido()) {
            throw new ValidacionException("codigo", "El código de un " + unidad.getTipo() + " empieza con "
                    + unidad.getPrefijoCodigo() + " y sigue con 8 dígitos (ej: " + unidad.getPrefijoCodigo() + "00000123)");
        }
        if (unidadDeVentaRepository.existsByCodigo(codigo)) {
            throw new ValidacionException("codigo", "Ya existe una unidad con ese código");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ValidacionException("username", "Ya existe un usuario con ese email");
        }

        // El responsable es un User con ROLE_RESPONSABLE (decisión de la cátedra), con la contraseña hasheada
        User responsable = new User(username, passwordEncoder.encode(dto.getPassword()), true);
        responsable.getUserRoles().add(new UserRole(null, responsable, ROL_RESPONSABLE));
        userRepository.save(responsable);

        unidad.setNombreComercial(dto.getNombreComercial().trim());
        unidad.setSuperficie(dto.getSuperficie());
        unidad.setResponsable(responsable);

        // La FK festival_id la escribe UnidadDeVenta.festival: la unidad apunta a su festival
        // y se guarda con su propio repositorio (es nueva, así que save() hace persist)
        unidad.setFestival(festival);
        return unidadDeVentaRepository.save(unidad);
    }

    // La subclase concreta según el tipo elegido: después se la trata como UnidadDeVenta
    private UnidadDeVenta crearUnidad(RegistroUnidadDTO dto) {
        if (dto.getTipoUnidad() == TipoUnidad.FOOD_TRUCK) {
            FoodTruck foodTruck = new FoodTruck();
            foodTruck.setPatente(dto.getPatente().trim().toUpperCase());
            foodTruck.setRequiereElectricidad(dto.isRequiereElectricidad());
            return foodTruck;
        }
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setCantCarpas(dto.getCantCarpas());
        puesto.setTiempoMontaje(dto.getTiempoMontaje());
        return puesto;
    }
}
