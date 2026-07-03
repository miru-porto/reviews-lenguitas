package com.lenguas.ratemyprof.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * DTO de salida de la búsqueda: agrupa las coincidencias por tipo.
 * Las coincidencias de profesor se expresan como sus cátedras (CatedraView)
 * porque la página destino de un resultado es la de la cátedra.
 */
@Data
@AllArgsConstructor
public class ResultadosBusqueda {
    private String consulta;
    private List<MateriaView> materias;
    private List<CatedraView> catedras;

    public boolean isVacio() {
        return materias.isEmpty() && catedras.isEmpty();
    }
}
