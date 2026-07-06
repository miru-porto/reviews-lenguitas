package com.lenguas.ratemyprof.exception;

import com.lenguas.ratemyprof.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores de la API: convierte excepciones en respuestas JSON con
 * el status correcto. El basePackages lo limita a los rest controllers; las
 * páginas Thymeleaf siguen usando el flujo de error normal de Spring.
 */
@RestControllerAdvice(basePackages = "com.lenguas.ratemyprof.controller.api")
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(e.getMessage()));
    }

    /**
     * Red de seguridad para todo lo demás. El mensaje real NO viaja al
     * cliente: puede filtrar detalles internos (SQL, rutas, versiones).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Error interno del servidor"));
    }
}
