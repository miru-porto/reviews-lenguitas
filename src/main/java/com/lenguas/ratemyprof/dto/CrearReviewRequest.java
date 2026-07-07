package com.lenguas.ratemyprof.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo JSON de POST /api/reviews. Como el REST identifica la cátedra en el
 * body (no en la URL, a diferencia del form Thymeleaf /review/nueva/{catedraId}),
 * incluye catedraId. Las mismas reglas de puntuación/comentario que ReviewForm.
 */
@Data
public class CrearReviewRequest {

    @NotNull(message = "Falta la cátedra")
    private Long catedraId;

    @NotNull(message = "Elegí una puntuación")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    private String comentario;
}
