package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.LoginRequest;
import com.lenguas.ratemyprof.dto.RegistroRequest;
import com.lenguas.ratemyprof.dto.UsuarioView;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticación de la API por DNI, sin contraseña. El DNI es la identidad: si
 * ya está registrado, ingresar; si no, el front muestra el alta (nombre/nick).
 * La sesión (cookie) sigue siendo el mecanismo: al ingresar guardamos el
 * SecurityContext en la sesión y las peticiones siguientes viajan autenticadas
 * con esa cookie.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final UsuarioService usuarioService;

    // Guarda/lee el SecurityContext en la HttpSession, igual que hace el
    // formLogin por dentro. Así el login por JSON queda "pegado" a la sesión.
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    /**
     * Ingreso por DNI. Si el DNI existe, inicia sesión y devuelve el usuario.
     * Si no existe, 404: el front lo interpreta como "DNI no registrado" y pasa
     * a la pantalla de alta (donde se reusa este mismo DNI).
     */
    @PostMapping("/login")
    public UsuarioView login(@Valid @RequestBody LoginRequest req,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        Usuario usuario = usuarioService.buscarPorDni(req.getDni())
                .orElseThrow(() -> new NotFoundException("DNI no registrado"));

        iniciarSesion(usuario, request, response);
        return UsuarioView.de(usuario);
    }

    /**
     * Alta de un usuario nuevo (DNI + nombre) que además queda logueado.
     * 201 con la vista del usuario; 409 si el DNI ya estaba registrado.
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioView> registro(@Valid @RequestBody RegistroRequest req,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        Usuario usuario = usuarioService.registrar(req.getDni(), req.getNombre());
        iniciarSesion(usuario, request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioView.de(usuario));
    }

    /** Logout: invalida la sesión y limpia el contexto. 204. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /**
     * Quién soy. Endpoint autenticado: sin sesión la filter chain corta con 401
     * (React lo lee como "no logueado"); con sesión, 200 con el usuario actual.
     */
    @GetMapping("/me")
    public UsuarioView me(Authentication auth) {
        return UsuarioView.de(usuarioService.findByDni(auth.getName()));
    }

    /**
     * Marca la sesión como autenticada para este usuario. Como no hay credenciales
     * que verificar, construimos directamente un Authentication ya autenticado
     * (el principal es el DNI, que es lo que devuelve auth.getName()) y lo
     * persistimos en la HttpSession. Las authorities salen del rol: con ellas
     * la filter chain puede exigir hasRole("ADMIN") en los endpoints de admin.
     */
    private void iniciarSesion(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getDni(), null,
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol().name()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
