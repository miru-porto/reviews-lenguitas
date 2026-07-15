package com.lenguas.ratemyprof.exception;

import com.lenguas.ratemyprof.dto.ApiError;
import com.lenguas.ratemyprof.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo de errores de la API: convierte excepciones en respuestas JSON con
 * el status correcto. El basePackages lo limita a los rest controllers; las
 * páginas Thymeleaf siguen usando el flujo de error normal de Spring.
 */
@RestControllerAdvice(basePackages = "com.lenguas.ratemyprof.controller.api")
public class ApiExceptionHandler {

    /** Entrada inválida que detecta el service, no Bean Validation (ej: cuatrimestre fuera de rango). */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(e.getMessage()));
    }

    /** Autenticado, pero sin permiso para esta acción (review ajena, voto propio). */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> forbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError(e.getMessage()));
    }

    /** Conflicto con el estado actual: review duplicada, DNI ya registrado. */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(e.getMessage()));
    }

    /**
     * Falló la validación de @Valid sobre un DTO de request: 400 con el detalle
     * por campo, para que el cliente sepa exactamente qué corregir.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            // Si un campo tiene varios errores, nos quedamos con el primero.
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationError("Datos inválidos", campos));
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
