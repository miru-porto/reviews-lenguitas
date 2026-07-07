package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/auth/login. Solo chequea que vengan los campos; si
 * las credenciales son incorrectas lo decide el AuthenticationManager (401).
 */
@Data
public class LoginRequest {

    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
