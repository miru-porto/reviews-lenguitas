package com.lenguas.ratemyprof.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * El usuario está autenticado pero no puede hacer esta acción (editar/borrar una
 * review ajena, votar la propia). Es la barrera de autorización del dominio, no
 * de Spring Security: la lanzan los services. El @ResponseStatus cubre el flujo
 * Thymeleaf (403); la API la mapea a JSON en ApiExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
