package com.lenguas.ratemyprof.config;

import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Se ejecuta cuando Google ya validó a la persona y nos devuelve sus datos.
 * Traduce esa identidad de Google a un usuario nuestro: lo busca por 'sub', lo
 * crea si es nuevo, y le calcula el rol.
 *
 * Delega en el OidcUserService de Spring para el trabajo sucio (validar el
 * id_token, pedir el userinfo) y solo se mete después, con los datos ya en mano.
 *
 * El OidcUser que devuelve lleva "sub" como nameAttributeKey: por eso
 * auth.getName() da el sub de Google en todo el resto de la app, igual que
 * antes daba el DNI.
 */
@Service
@RequiredArgsConstructor
public class GoogleOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UsuarioService usuarioService;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = delegate.loadUser(userRequest);

        String googleSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        Usuario usuario = usuarioService.ingresarConGoogle(googleSub, email);

        // Las authorities salen de NUESTRO rol, no de Google: Google dice quién
        // sos, nosotros decidimos qué podés hacer.
        return new DefaultOidcUser(
                UsuarioService.authorities(usuario),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "sub");
    }
}
