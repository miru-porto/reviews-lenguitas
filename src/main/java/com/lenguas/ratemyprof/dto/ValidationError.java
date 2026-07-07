package com.lenguas.ratemyprof.dto;

import lombok.Data;

import java.util.Map;

/**
 * Cuerpo JSON de un 400 por validación: además del mensaje general reutiliza la
 * clave "error" (como ApiError) y agrega "campos" con el detalle por campo
 * (nombre → mensaje), para que React pueda pintar el error debajo de cada input.
 */
@Data
public class ValidationError {

    private final String error;
    private final Map<String, String> campos;
}
