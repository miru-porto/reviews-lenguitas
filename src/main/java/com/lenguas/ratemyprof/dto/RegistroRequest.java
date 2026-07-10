package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/auth/registro. Alta de un usuario nuevo: solo DNI
 * (su identidad) y nombre (el nick con el que aparecen sus reviews). No hay
 * email ni contraseña en el alta.
 */
@Data
public class RegistroRequest {

    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "\\d{7,8}", message = "El DNI debe tener 7 u 8 dígitos")
    private String dni;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;
}
