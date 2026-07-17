package com.lenguas.ratemyprof.dto;

import com.lenguas.ratemyprof.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta JSON de /me y del alta de apodo. Es la vista pública del usuario:
 * id, apodo y rol. React la guarda en su AuthContext para saber quién está
 * logueado; con el rol decide si muestra la sección de administración (la
 * protección real está en el backend: esto es solo UI).
 *
 * NO expone el email ni el id de Google: la app no los necesita para dibujar
 * nada, y lo que no se manda no se puede filtrar. `nombre` viene null mientras
 * la persona no eligió apodo — así el front sabe que tiene que pedirlo.
 */
@Data
@AllArgsConstructor
public class UsuarioView {

    private Long id;
    private String nombre;
    private String rol;

    public static UsuarioView de(Usuario usuario) {
        return new UsuarioView(
                usuario.getId(), usuario.getNombre(), usuario.getRol().name());
    }
}
