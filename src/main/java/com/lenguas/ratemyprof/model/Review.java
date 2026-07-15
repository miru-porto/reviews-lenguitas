package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catedra_id", nullable = false)
    private Catedra catedra;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer puntuacion;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comentario;

    /**
     * Cuatrimestre en que el usuario cursó, ej: "1C 2025" (ver Cuatrimestre).
     * Obligatorio al crear/editar (lo exige el borde web), pero nullable en la
     * base: las reviews anteriores a este campo no lo tienen. Se endurecerá
     * con una migración Flyway cuando se backfilleen esas filas.
     */
    @Column(length = 7)
    private String cuatrimestre;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
