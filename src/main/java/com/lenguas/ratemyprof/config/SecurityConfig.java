package com.lenguas.ratemyprof.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
 * Dos filter chains: una para la API (/api/**) y otra para las páginas
 * Thymeleaf. Se ordenan; Spring prueba la de @Order más bajo primero. La de la
 * API tiene securityMatcher("/api/**"); la web no lo tiene, así que atrapa todo
 * lo que la API no reclamó.
 *
 * ¿Por qué dos? La API y la web se defienden distinto:
 *  - API: sin login responde 401 en JSON (no redirige a /login), CORS para
 *    React, y CSRF vía cookie legible (patrón SPA).
 *  - Web: formLogin clásico, redirecciones, CSRF por token oculto en los forms.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Necesario para el login por JSON: AuthApiController lo usa para autenticar
     * email+password. Spring lo arma con nuestro UserDetailsService (UsuarioService)
     * y el PasswordEncoder de arriba.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // -------------------- Filter chain de la API --------------------

    @Bean
    @Order(1)
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
                // Alta de sesión (registro/login) sin login previo.
                .requestMatchers("/api/auth/registro", "/api/auth/login").permitAll()
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
            .exceptionHandling(ex -> ex
                // Sin sesión → 401 en vez de redirigir a /login.
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                // Con sesión pero sin permiso a nivel filtro → 403.
                .accessDeniedHandler((request, response, denied) ->
                        response.sendError(HttpStatus.FORBIDDEN.value()))
            );

        return http.build();
    }

    // -------------------- Filter chain de las páginas Thymeleaf --------------------

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas: home, ver materias, ver reviews, buscar.
                // "/error" es el dispatch interno de Spring cuando salta una excepción:
                // si Security lo bloquea, un 404 termina redirigiendo al login.
                .requestMatchers("/", "/materias", "/materias/**", "/catedra/**", "/buscar", "/registro", "/css/**", "/error").permitAll()
                // Crear review requiere estar logueado
                .requestMatchers("/review/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/materias")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
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
