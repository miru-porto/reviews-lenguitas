package com.lenguas.ratemyprof.model;

/**
 * Rol del usuario. USER es el default (puede escribir reviews y votar);
 * ADMIN además administra el catálogo (materias, profesores, cátedras).
 * Spring Security lo consume como authority "ROLE_USER" / "ROLE_ADMIN":
 * el prefijo "ROLE_" es la convención que espera hasRole().
 */
public enum Rol {
    USER,
    ADMIN
}
