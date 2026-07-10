package com.lenguas.ratemyprof.dto;

import com.lenguas.ratemyprof.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Respuesta JSON de registro, login y /me. Es la vista pública del usuario:
 * expone id/dni/nombre. React la guarda en su AuthContext para saber quién
 * está logueado.
 */
@Data
@AllArgsConstructor
public class UsuarioView {

    private Long id;
    private String dni;
    private String nombre;

    public static UsuarioView de(Usuario usuario) {
        return new UsuarioView(usuario.getId(), usuario.getDni(), usuario.getNombre());
    }
}
