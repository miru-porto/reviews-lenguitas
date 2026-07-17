package com.lenguas.ratemyprof.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Seguridad de la API REST (/api/**). El backend ya no sirve páginas: el front
 * es React (dev server aparte, o el build estático servido por este mismo app).
 *
 * Una sola filter chain, con securityMatcher("/api/**"): las peticiones que no
 * son de la API (los archivos del build de React, sus rutas de cliente) no
 * pasan por Security y se sirven como recursos públicos.
 *
 * La API se defiende como una SPA: sin sesión responde 401 en JSON (no redirige
 * a ningún /login), habilita CORS para el dev server de React y usa CSRF con
 * cookie legible. La sesión (cookie) la inicia AuthApiController por DNI.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final GoogleOidcUserService googleOidcUserService;

    @Value("${app.front-url:http://localhost:5173}")
    private String frontUrl;

    public SecurityConfig(GoogleOidcUserService googleOidcUserService) {
        this.googleOidcUserService = googleOidcUserService;
    }

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        // Handler que deja el token CSRF "en claro" (sin el enmascarado XOR anti-BREACH),
        // para que el valor de la cookie XSRF-TOKEN coincida con el del header que
        // manda React. Es la variante recomendada para clientes JavaScript.
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
            .securityMatcher("/api/**")
            .cors(cors -> {})
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler)
            )
            // Escribe la cookie XSRF-TOKEN en cada request (ver CsrfCookieFilter).
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // El baile con Google: arrancar el login y recibir la vuelta.
                // Son públicos por definición (todavía no hay sesión).
                .requestMatchers("/api/oauth2/**", "/api/login/oauth2/**").permitAll()
                // /me pide sesión: sin ella responde 401, y así React sabe que no
                // hay login. Va ANTES del permitAll de GET (gana el primer match).
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                // Resto de la lectura, pública.
                .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                // Escritura del catálogo (materias/profesores/cátedras), solo ADMIN.
                // Los GET de estas rutas ya matchearon el permitAll de arriba, así
                // que esta regla solo atrapa POST/PUT/DELETE. hasRole("ADMIN")
                // busca la authority "ROLE_ADMIN" (el prefijo lo agrega Spring).
                .requestMatchers("/api/materias/**", "/api/profesores/**", "/api/catedras/**").hasRole("ADMIN")
                // Todo lo demás (crear/editar/borrar, logout) pide sesión.
                .anyRequest().authenticated()
            )
            // ---- Login con Google ----
            // Los tres endpoints cuelgan de /api/** a propósito, y no de las rutas
            // por defecto de Spring (/oauth2/**, /login/oauth2/**): el rewrite de
            // Vercel solo reenvía /api/* al backend, así que metiéndolos ahí el
            // baile entero pasa por el proxy y la cookie sigue siendo first-party.
            // Si se movieran fuera de /api, el callback de Google moriría en el
            // fallback de la SPA (index.html) en vez de llegar a Spring.
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(a -> a.baseUri("/api/oauth2/authorization"))
                .redirectionEndpoint(r -> r.baseUri("/api/login/oauth2/code/*"))
                .userInfoEndpoint(u -> u.oidcUserService(googleOidcUserService))
                // Terminado el login, Google nos devuelve acá y nosotros mandamos
                // a la persona al front. Absoluto porque en producción el front
                // vive en otro dominio (Vercel) que el backend (Render).
                .defaultSuccessUrl(frontUrl + "/", true)
                .failureUrl(frontUrl + "/login?error=google")
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, authentication) ->
                        res.setStatus(HttpStatus.NO_CONTENT.value()))
                .deleteCookies("JSESSIONID")
            )
            .exceptionHandling(ex -> ex
                // Sin sesión → 401 en vez de redirigir a Google. El front decide
                // cuándo mandarte a login; la API nunca redirige sola.
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                // Con sesión pero sin permiso a nivel filtro → 403.
                .accessDeniedHandler((request, response, denied) ->
                        response.sendError(HttpStatus.FORBIDDEN.value()))
            );

        return http.build();
    }

    // -------------------- CORS para React --------------------

    /**
     * Permite que el front de React (otro origen, otro puerto) llame a la API
     * con la cookie de sesión. allowCredentials=true es imprescindible para que
     * el navegador mande la cookie; por eso el origen debe ser explícito (no "*").
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
