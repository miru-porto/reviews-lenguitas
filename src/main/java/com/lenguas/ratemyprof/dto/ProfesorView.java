package com.lenguas.ratemyprof.dto;

import com.lenguas.ratemyprof.model.Profesor;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Vista JSON de un profesor (lista pública y pantalla de admin). Se mapea a
 * DTO por la razón de siempre: la entidad expone la relación LAZY a cátedras.
 */
@Data
@AllArgsConstructor
public class ProfesorView {

    private Long id;
    private String nombre;
    private String apellido;

    public static ProfesorView de(Profesor profesor) {
        return new ProfesorView(profesor.getId(), profesor.getNombre(), profesor.getApellido());
    }
}
