package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de entrada del formulario de reviews. Concentra la validación en el borde
 * web, antes de que los datos lleguen al service o a la base.
 */
@Data
public class ReviewForm {

    @NotNull(message = "Elegí una puntuación")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    private String comentario;
}
