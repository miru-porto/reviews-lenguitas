package com.lenguas.ratemyprof.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CatedraConRating {
    private Long catedraId;
    private String nombreProfesor;
    private String apellidoProfesor;
    private String nombreMateria;
    private Double promedioRating;
    private Long cantidadReviews;
}
