package com.lenguas.ratemyprof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Entrada inválida que Bean Validation no puede expresar en el DTO (ej: un
 * cuatrimestre con formato correcto pero fuera del rango 2018..hoy). El
 * @ResponseStatus hace que Spring responda 400 aunque nadie la capture.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
