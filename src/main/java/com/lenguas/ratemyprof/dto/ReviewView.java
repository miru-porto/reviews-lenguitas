package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de salida (view model) de una review. Lleva solo lo que la vista necesita,
 * con la fecha ya formateada, para no exponer la entidad JPA ni sus relaciones LAZY.
 */
@Data
@AllArgsConstructor
public class ReviewView {
    private String autor;
    private Integer puntuacion;
    private String comentario;
    private String fecha;
}
