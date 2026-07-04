package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Voto "útil" de un usuario sobre una review. Tabla de unión usuario↔review:
 * el constraint único evita que el mismo usuario vote dos veces la misma review
 * (misma idea que el anti-duplicado de reviews por cátedra).
 */
@Entity
@Table(name = "votos_util", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"usuario_id", "review_id"})
})
@Data
@NoArgsConstructor
public class VotoUtil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
