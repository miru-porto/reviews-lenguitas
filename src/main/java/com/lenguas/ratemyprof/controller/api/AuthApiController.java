package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.LoginRequest;
import com.lenguas.ratemyprof.dto.RegistroRequest;
import com.lenguas.ratemyprof.dto.UsuarioView;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
 * Autenticación de la API sin formulario HTML. En vez del formLogin de Thymeleaf,
 * React manda JSON a estos endpoints. La sesión (cookie) sigue siendo el
 * mecanismo: al hacer login guardamos el SecurityContext en la sesión y las
 * peticiones siguientes viajan autenticadas con esa cookie.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;

    // Guarda/lee el SecurityContext en la HttpSession, igual que hace el
    // formLogin por dentro. Así el login por JSON queda "pegado" a la sesión.
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    /** Registro. 201 con la vista pública del usuario (sin password). */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioView> registro(@Valid @RequestBody RegistroRequest req) {
        // registrar lanza ConflictException (409) si el email ya existe.
        Usuario usuario = usuarioService.registrar(req.getNombre(), req.getEmail(), req.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioView.de(usuario));
    }

    /**
     * Login. authenticate() valida email+password contra UsuarioService +
     * PasswordEncoder; si fallan lanza AuthenticationException → 401 (lo mapea
     * ApiExceptionHandler). Si andan, persistimos el contexto en la sesión.
     */
    @PostMapping("/login")
    public UsuarioView login(@Valid @RequestBody LoginRequest req,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return UsuarioView.de(usuarioService.findByEmail(authentication.getName()));
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
        return UsuarioView.de(usuarioService.findByEmail(auth.getName()));
    }
}
