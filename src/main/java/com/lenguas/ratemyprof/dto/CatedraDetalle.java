package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta de GET /api/catedras/{id}: el encabezado de la cátedra más su
 * desglose de rating, juntos para que el cliente resuelva la página con un
 * solo request.
 */
@Data
@AllArgsConstructor
public class CatedraDetalle {
    private CatedraView catedra;
    private RatingBreakdown rating;
}
