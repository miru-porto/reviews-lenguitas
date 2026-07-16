package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/materias y PUT /api/materias/{id} (admin).
 * El mismo DTO sirve para crear y editar.
 */
@Data
public class MateriaRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    private String nombre;

    @NotNull(message = "Elegí el año de cursada")
    @Min(value = 1, message = "El año debe estar entre 1 y 5")
    @Max(value = 5, message = "El año debe estar entre 1 y 5")
    private Integer anio;
}
