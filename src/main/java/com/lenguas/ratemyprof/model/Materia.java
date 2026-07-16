package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "materias")
@Data
@NoArgsConstructor
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nombre;

    /**
     * Año de cursada sugerido por el plan de estudios (1 a 5; el 5to existe
     * solo en el plan de 5 años). Nullable en la base por las materias
     * anteriores al campo; se endurecerá con la migración Flyway.
     */
    @Column(name = "anio")
    private Integer anio;

    @OneToMany(mappedBy = "materia")
    private List<Catedra> catedras;
}
