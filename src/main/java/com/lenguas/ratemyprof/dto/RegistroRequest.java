package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/auth/registro. La validación vive acá, en el borde:
 * el form Thymeleaf recibía @RequestParam sueltos sin validar.
 */
@Data
public class RegistroRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @Email(message = "El email no es válido")
    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
