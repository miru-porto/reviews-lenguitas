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
    /**
     * Si el usuario logueado ya dejó review acá (false sin sesión). Con la
     * lista paginada el cliente no puede deducirlo: su review puede estar en
     * una página que no cargó.
     */
    private boolean yaReviewe;
}
