package com.unla.epicentrogourmet.dtos;

import java.math.BigDecimal;

import com.unla.epicentrogourmet.enums.TipoUnidad;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Datos del formulario público de registro. No es una entidad: con esto el service arma
// la unidad (FoodTruck o PuestoDesarmable) y el User del responsable
@Getter @Setter @NoArgsConstructor
public class RegistroUnidadDTO {

    // ---- Unidad de venta
    @NotNull(message = "Elegí el tipo de unidad")
    private TipoUnidad tipoUnidad;

    @NotNull(message = "Elegí el festival")
    private Integer festivalId;

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 100, message = "El nombre comercial admite hasta 100 caracteres")
    private String nombreComercial;

    // Acá solo el largo: el formato (prefijo del tipo + 8 dígitos) lo valida la propia unidad
    @NotBlank(message = "El código es obligatorio")
    @Size(min = 10, max = 10, message = "El código tiene que tener exactamente 10 caracteres")
    private String codigo;

    // La columna es decimal(8,2): hasta 6 enteros y 2 decimales
    @NotNull(message = "La superficie es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La superficie tiene que ser mayor a 0")
    @Digits(integer = 6, fraction = 2, message = "La superficie admite hasta 6 enteros y 2 decimales")
    private BigDecimal superficie;

    // ---- Solo Food Truck
    // Patente vieja (ABC123) o Mercosur (AB123CD). Vacía pasa acá: si es food truck la exige isPatenteCompleta()
    @Pattern(regexp = "^$|^[A-Za-z]{3}\\d{3}$|^[A-Za-z]{2}\\d{3}[A-Za-z]{2}$",
            message = "La patente tiene que tener el formato ABC123 o AB123CD")
    private String patente;

    private boolean requiereElectricidad;

    // ---- Solo Puesto Desarmable
    @Min(value = 1, message = "Tiene que tener al menos una carpa")
    private Integer cantCarpas;

    @Min(value = 1, message = "El tiempo de montaje tiene que ser de al menos 1 minuto")
    private Integer tiempoMontaje;

    // ---- Responsable (username = email, como acordó la cátedra)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email admite hasta 100 caracteres")
    private String username;

    // BCrypt ignora lo que pase de 72 bytes, por eso el máximo
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña tiene que tener entre 8 y 72 caracteres")
    private String password;

    @NotBlank(message = "Repetí la contraseña")
    private String confirmarPassword;

    // ---- Validaciones que cruzan más de un campo
    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordsCoinciden() {
        return password == null || password.equals(confirmarPassword);
    }

    @AssertTrue(message = "La patente es obligatoria para un food truck")
    public boolean isPatenteCompleta() {
        return tipoUnidad != TipoUnidad.FOOD_TRUCK || (patente != null && !patente.isBlank());
    }

    @AssertTrue(message = "Las carpas y el tiempo de montaje son obligatorios para un puesto desarmable")
    public boolean isDatosPuestoCompletos() {
        return tipoUnidad != TipoUnidad.PUESTO_DESARMABLE || (cantCarpas != null && tiempoMontaje != null);
    }
}
