package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de salida (view model) del encabezado de la página de reviews de una cátedra.
 * Aplana profesor y materia para que la vista no navegue relaciones LAZY de la entidad.
 */
@Data
@AllArgsConstructor
public class CatedraView {
    private Long catedraId;
    private Long materiaId;
    private String materiaNombre;
    private String nombreProfesor;
    private String apellidoProfesor;
}
