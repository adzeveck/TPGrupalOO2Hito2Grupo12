package com.unla.epicentrogourmet.services.implementation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unla.epicentrogourmet.dtos.CambioPasswordDTO;
import com.unla.epicentrogourmet.entities.User;
import com.unla.epicentrogourmet.exceptions.ValidacionException;
import com.unla.epicentrogourmet.repositories.IUserRepository;
import com.unla.epicentrogourmet.services.ICuentaService;

@Service("cuentaService")
public class CuentaService implements ICuentaService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void cambiarPassword(String username, CambioPasswordDTO cambio) {
        // El usuario sale de la sesión: nadie puede cambiar la contraseña de otro
        User usuario = userRepository.findByUsernameAndFetchUserRolesEagerly(username);
        if (usuario == null) {
            throw new ValidacionException(null, "No se encontró tu usuario");
        }
        // En la base solo hay hashes BCrypt: se compara con matches, nunca con equals
        if (!passwordEncoder.matches(cambio.getPasswordActual(), usuario.getPassword())) {
            throw new ValidacionException("passwordActual", "La contraseña actual no es correcta");
        }
        if (passwordEncoder.matches(cambio.getPasswordNueva(), usuario.getPassword())) {
            throw new ValidacionException("passwordNueva", "La nueva contraseña tiene que ser distinta de la actual");
        }
        usuario.setPassword(passwordEncoder.encode(cambio.getPasswordNueva()));
        userRepository.save(usuario);
    }
}