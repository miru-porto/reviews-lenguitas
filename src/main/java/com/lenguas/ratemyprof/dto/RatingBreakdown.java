package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Desglose de rating de una cátedra: el promedio, el total de reviews y la
 * distribución por cantidad de estrellas (niveles ordenados de 5 a 1).
 */
@Data
@AllArgsConstructor
public class RatingBreakdown {
    private Double promedio;
    private long total;
    private List<NivelRating> niveles;
}
