package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Cuerpo del 201 de POST /api/reviews: el id de la review nueva y el de su
 * cátedra, para que React pueda navegar/refetchear sin adivinar.
 */
@Data
@AllArgsConstructor
public class ReviewCreadaResponse {

    private Long id;
    private Long catedraId;
}
