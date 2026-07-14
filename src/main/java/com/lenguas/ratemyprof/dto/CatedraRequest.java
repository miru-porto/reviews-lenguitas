package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/catedras (admin). Una cátedra es el par
 * profesor+materia, así que crear una es elegir ambos por id. No hay PUT:
 * "cambiar el profesor" de una cátedra sería otra cátedra (se borra y se
 * crea la nueva).
 */
@Data
public class CatedraRequest {

    @NotNull(message = "Falta el profesor")
    private Long profesorId;

    @NotNull(message = "Falta la materia")
    private Long materiaId;
}
