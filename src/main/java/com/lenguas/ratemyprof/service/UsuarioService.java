package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.model.Rol;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Email que recibe rol ADMIN al entrar. Va por variable de entorno y no
     * hardcodeado: el repo es público y no queremos publicar un mail personal,
     * y así se cambia sin migración ni deploy de código. Vacío = nadie es admin.
     */
    @Value("${app.admin-email:}")
    private String adminEmail;

    /**
     * Punto de entrada del login con Google: busca al usuario por su 'sub' y si
     * no existe lo crea. Se llama en cada ingreso (ver GoogleOidcUserService),
     * así que también sirve para refrescar datos que hayan cambiado del lado de
     * Google (el email) y para recalcular el rol.
     *
     * El apodo NO se completa acá: queda null a propósito y el front pide uno
     * antes de dejar reseñar (ver Usuario.nombre).
     */
    @Transactional
    public Usuario ingresarConGoogle(String googleSub, String email) {
        Usuario usuario = usuarioRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setGoogleSub(googleSub);
                    return nuevo;
                });

        usuario.setEmail(email);
        usuario.setRol(esAdmin(email) ? Rol.ADMIN : Rol.USER);

        return usuarioRepository.save(usuario);
    }

    /**
     * El rol se decide en CADA ingreso a partir del email, no se guarda a mano:
     * si mañana cambia ADMIN_EMAIL, el admin viejo deja de serlo al reingresar
     * sin que haya que tocar la base.
     */
    private boolean esAdmin(String email) {
        return adminEmail != null && !adminEmail.isBlank()
                && adminEmail.equalsIgnoreCase(email);
    }

    /** Elige el apodo público. Es lo único que se muestra junto a una review. */
    @Transactional
    public Usuario elegirApodo(String googleSub, String apodo) {
        Usuario usuario = findByGoogleSub(googleSub);
        usuario.setNombre(apodo);
        return usuarioRepository.save(usuario);
    }

    /** Busca por el id de Google sin fallar. */
    public Optional<Usuario> buscarPorGoogleSub(String googleSub) {
        return usuarioRepository.findByGoogleSub(googleSub);
    }

    /** Igual que el anterior pero exige que exista (usuario ya autenticado). */
    public Usuario findByGoogleSub(String googleSub) {
        return usuarioRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    /** Authorities de Spring Security para este usuario ("ROLE_USER"/"ROLE_ADMIN"). */
    public static java.util.List<org.springframework.security.core.GrantedAuthority> authorities(Usuario usuario) {
        return AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol().name());
    }
}
