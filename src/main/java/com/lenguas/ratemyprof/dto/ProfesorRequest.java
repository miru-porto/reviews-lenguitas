package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/profesores y PUT /api/profesores/{id} (admin).
 */
@Data
public class ProfesorRequest {

    /** Opcional: de muchos profesores solo se conoce el apellido (ver Profesor). */
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;
}
