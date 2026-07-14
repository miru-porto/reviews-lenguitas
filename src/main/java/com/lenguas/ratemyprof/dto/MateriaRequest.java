package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/materias y PUT /api/materias/{id} (admin).
 * Una materia es solo su nombre; el mismo DTO sirve para crear y editar.
 */
@Data
public class MateriaRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    private String nombre;
}
