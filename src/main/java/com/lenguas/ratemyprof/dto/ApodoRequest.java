package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Cuerpo de PUT /api/auth/apodo: el nombre público que se ve en las reviews. */
@Data
public class ApodoRequest {

    @NotBlank(message = "El apodo no puede estar vacío")
    @Size(min = 2, max = 40, message = "El apodo debe tener entre 2 y 40 caracteres")
    private String apodo;
}
