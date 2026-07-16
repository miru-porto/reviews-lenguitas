package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de salida (view model) de una materia: solo lo que la vista necesita
 * para listar y linkear, sin exponer la entidad JPA ni su lista de cátedras.
 */
@Data
@AllArgsConstructor
public class MateriaView {
    private Long id;
    private String nombre;
    /** Año de cursada según el plan (1..5). Null en materias sin año asignado. */
    private Integer anio;
}
