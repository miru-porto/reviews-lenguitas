package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/auth/login. El login es solo por DNI: si el DNI
 * existe se inicia sesión, y si no, el controller responde 404 para que el
 * front ofrezca registrarse.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos")
    private String dni;
}
