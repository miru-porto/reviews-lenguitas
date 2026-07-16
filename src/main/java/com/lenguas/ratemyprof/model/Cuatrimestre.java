package com.lenguas.ratemyprof.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * El "cuatrimestre cursado" de una review, con formato "1C 2025" / "2C 2025".
 * No es una entidad: es el único lugar que define qué valores son válidos,
 * para que la API y la validación usen exactamente la misma lista. El front
 * React genera las mismas opciones por su cuenta
 * (frontend/src/utils/cuatrimestres.js): si se cambia la regla acá hay que
 * cambiarla allá también.
 */
public final class Cuatrimestre {

    /** Primer cuatrimestre elegible: no se aceptan cursadas anteriores a 2018. */
    public static final int PRIMER_ANIO = 2018;

    private Cuatrimestre() {
    }

    /**
     * Opciones válidas, de la más reciente a la más vieja ("2C 2026" ... "1C 2018").
     * El tope superior es el cuatrimestre en curso: el 2C del año actual se
     * habilita desde julio, cuando la cursada del 1C ya terminó.
     */
    public static List<String> opciones() {
        LocalDate hoy = LocalDate.now();
        int ultimoCuatri = hoy.getMonthValue() >= 7 ? 2 : 1;
        List<String> opciones = new ArrayList<>();
        for (int anio = hoy.getYear(); anio >= PRIMER_ANIO; anio--) {
            for (int c = (anio == hoy.getYear() ? ultimoCuatri : 2); c >= 1; c--) {
                opciones.add(c + "C " + anio);
            }
        }
        return opciones;
    }

    /**
     * ¿El valor es una opción válida? Compara contra la lista en vez de usar una
     * regex: así el rango (2018..hoy) y el formato quedan definidos una sola vez.
     */
    public static boolean esValido(String valor) {
        return valor != null && opciones().contains(valor);
    }
}
