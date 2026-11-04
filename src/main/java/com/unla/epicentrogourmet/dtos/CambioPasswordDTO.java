package com.unla.epicentrogourmet.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lo que carga el usuario al cambiar su contraseña. Es un DTO de entrada: no se persiste
@Getter @Setter @NoArgsConstructor
public class CambioPasswordDTO {

    @NotBlank(message = "Ingresá tu contraseña actual")
    private String passwordActual;

    // BCrypt ignora lo que pase de 72 bytes, por eso el máximo
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña tiene que tener entre 8 y 72 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Repetí la nueva contraseña")
    private String confirmarPassword;

    // ---- Validación que cruza más de un campo
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordsCoinciden() {
        return passwordNueva == null || passwordNueva.equals(confirmarPassword);
    }
}