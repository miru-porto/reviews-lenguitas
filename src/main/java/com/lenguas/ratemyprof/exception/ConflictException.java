package com.lenguas.ratemyprof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * La petición choca con una regla de negocio sobre el estado actual: ya existe
 * una review del usuario en esa cátedra, o ya hay una cuenta con ese email. No
 * es un error de datos (400) sino un conflicto con lo que ya está guardado (409).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
