package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.model.Rol;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import com.lenguas.ratemyprof.repository.VotoUtilRepository;
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
    private final ReviewRepository reviewRepository;
    private final VotoUtilRepository votoUtilRepository;

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

    /**
     * Baja de cuenta: borra a la persona y todo lo que dejó. Es definitivo y no
     * hay papelera — es lo que promete la política de privacidad.
     *
     * El orden lo manda el grafo de claves foráneas, que a propósito no borra en
     * cascada (así nada desaparece por accidente desde otro lado):
     *   1. los votos que OTROS dejaron en mis reviews — si no, las reviews no salen;
     *   2. los votos que yo dejé en reviews ajenas;
     *   3. mis reviews;
     *   4. yo.
     * Todo en una transacción: si algo falla, no queda media cuenta borrada.
     *
     * Se van también las reviews, y eso le cuesta contenido al sitio. Es
     * deliberado: alguien que pide borrar sus datos espera que desaparezcan, no
     * que sus opiniones sigan publicadas bajo un "usuario eliminado".
     */
    @Transactional
    public void borrarCuenta(String googleSub) {
        Usuario usuario = findByGoogleSub(googleSub);

        votoUtilRepository.borrarVotosSobreReviewsDe(usuario.getId());
        votoUtilRepository.borrarVotosDe(usuario.getId());
        reviewRepository.borrarReviewsDe(usuario.getId());
        usuarioRepository.delete(usuario);
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
