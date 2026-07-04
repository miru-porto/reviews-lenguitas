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
    private Long id;
    private String autor;
    private Integer puntuacion;
    private String comentario;
    private String fecha;
    /** true si la review pertenece al usuario logueado (para mostrarle editar/borrar). */
    private boolean esMia;
    /** cantidad de usuarios que marcaron esta review como útil. */
    private long votosUtil;
    /** true si el usuario logueado ya la votó como útil (para togglear el botón). */
    private boolean laVoteUtil;
}
