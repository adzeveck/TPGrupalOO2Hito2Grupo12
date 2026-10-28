package com.unla.epicentrogourmet.dtos;

import java.time.LocalDate;

import com.unla.epicentrogourmet.enums.CategoriaCocina;
import com.unla.epicentrogourmet.enums.Turno;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter @Setter @NoArgsConstructor
public class EmpleadoFormDTO {

    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido admite hasta 50 caracteres")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{7,10}", message = "El DNI debe tener entre 7 y 10 dígitos")
    private String dni;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresá un correo válido")
    @Size(max = 100, message = "El correo admite hasta 100 caracteres")
    private String email;

    @NotBlank(message = "Elegí el tipo de empleado")
    private String tipoEmpleado;

    private CategoriaCocina categoria;
    @Size(max = 100, message = "La especialidad admite hasta 100 caracteres")
    private String especialidad;
    private Turno turno;

    @AssertTrue(message = "Elegí una categoría para el cocinero")
    public boolean isCategoriaValida() {
        return !"COCINERO".equals(tipoEmpleado) || categoria != null;
    }

    @AssertTrue(message = "Elegí un turno para el cajero")
    public boolean isTurnoValido() {
        return !"CAJERO".equals(tipoEmpleado) || turno != null;
    }

    @AssertTrue(message = "El tipo de empleado debe ser cocinero o cajero")
    public boolean isTipoEmpleadoValido() {
        return "COCINERO".equals(tipoEmpleado) || "CAJERO".equals(tipoEmpleado);
    }

    @AssertTrue(message = "El empleado tiene que ser mayor de edad")
    public boolean isMayorDeEdad() {
        return fechaNacimiento == null
                || !fechaNacimiento.plusYears(18).isAfter(LocalDate.now());
    }
}
