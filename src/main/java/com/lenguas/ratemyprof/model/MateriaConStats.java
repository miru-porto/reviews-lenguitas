package com.lenguas.ratemyprof.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Materia con sus agregados para la portada: rating promedio, cuántas
 * cátedras tiene y cuántas reviews juntan entre todas.
 *
 * El promedio es sobre las reviews de la materia, no el promedio de los
 * promedios de sus cátedras: una cátedra con 50 reviews pesa más que una
 * con 2. Vale 0.0 cuando todavía no hay ninguna review (la portada muestra
 * "sin reviews" en ese caso, no un 0 que parecería una nota pésima).
 */
@Data
@AllArgsConstructor
public class MateriaConStats {
    private Long id;
    private String nombre;
    /** Año de cursada según el plan (1..5). Null en materias sin año asignado. */
    private Integer anio;
    private Double promedioRating;
    private Long cantidadCatedras;
    private Long cantidadReviews;
}
