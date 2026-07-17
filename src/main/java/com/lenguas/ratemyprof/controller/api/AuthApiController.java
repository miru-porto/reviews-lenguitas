package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.ApodoRequest;
import com.lenguas.ratemyprof.dto.UsuarioView;
import com.lenguas.ratemyprof.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lo que queda de la auth después de pasar a Google.
 *
 * Ya no hay /login ni /registro: el ingreso lo maneja entero Spring Security
 * contra Google (ver SecurityConfig.oauth2Login), y el alta del usuario ocurre
 * sola en el primer ingreso (ver GoogleOidcUserService). El /logout también es
 * de Spring Security ahora. Acá solo queda "quién soy" y elegir el apodo.
 *
 * En todos los métodos auth.getName() devuelve el 'sub' de Google: es el
 * principal que puso GoogleOidcUserService, y es la identidad del usuario.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UsuarioService usuarioService;

    /**
     * Quién soy. Endpoint autenticado: sin sesión la filter chain corta con 401
     * (React lo lee como "no logueada"); con sesión, 200 con el usuario actual.
     * Si `nombre` viene null, el front manda a elegir apodo.
     */
    @GetMapping("/me")
    public UsuarioView me(Authentication auth) {
        return UsuarioView.de(usuarioService.findByGoogleSub(auth.getName()));
    }

    /**
     * Elige (o cambia) el apodo público. Requiere sesión. Es un PUT y no un POST
     * porque sirve para las dos cosas: estrenarlo y editarlo después.
     */
    @PutMapping("/apodo")
    public UsuarioView apodo(@Valid @RequestBody ApodoRequest req, Authentication auth) {
        return UsuarioView.de(usuarioService.elegirApodo(auth.getName(), req.getApodo()));
    }
}
