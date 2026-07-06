package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Cuerpo JSON de un error de la API: {"error": "..."}. Un formato único para
 * todos los errores hace que el cliente (React) siempre sepa dónde leer el
 * mensaje.
 */
@Data
@AllArgsConstructor
public class ApiError {
    private String error;
}
