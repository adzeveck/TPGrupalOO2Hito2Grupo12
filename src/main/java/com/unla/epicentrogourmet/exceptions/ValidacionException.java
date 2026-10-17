package com.unla.epicentrogourmet.exceptions;

import lombok.Getter;

// Una regla de negocio que no se cumple. Lleva el campo del formulario
// para que el controller pueda mostrar el error al lado de ese campo
@Getter
public class ValidacionException extends RuntimeException {

    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }
}

