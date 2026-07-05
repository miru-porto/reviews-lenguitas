package com.lenguas.ratemyprof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Recurso inexistente (cátedra, review, materia). El @ResponseStatus hace que
 * Spring responda 404 en vez de 500 aunque nadie la capture; falta mapear a JSON.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
