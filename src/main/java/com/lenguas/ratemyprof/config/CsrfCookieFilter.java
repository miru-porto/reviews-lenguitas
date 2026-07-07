package com.lenguas.ratemyprof.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Fuerza que el token CSRF se "materialice" en cada request. Con
 * CookieCsrfTokenRepository el token es diferido: la cookie XSRF-TOKEN solo se
 * escribe cuando alguien lee el token. React necesita esa cookie desde el
 * primer GET (p. ej. /api/auth/me) para poder mandarla luego en el header
 * X-XSRF-TOKEN de los POST/PUT/DELETE. Llamar a getToken() dispara el Set-Cookie.
 *
 * Es el patrón recomendado por la guía de Spring Security para SPAs.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken(); // dispara la escritura de la cookie
        }
        filterChain.doFilter(request, response);
    }
}
