package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Un escalón del desglose de rating: cuántas reviews tiene cierta cantidad de
 * estrellas y qué porcentaje representa sobre el total (para dibujar la barra).
 */
@Data
@AllArgsConstructor
public class NivelRating {
    private int estrellas;
    private long cantidad;
    private int porcentaje;
}
