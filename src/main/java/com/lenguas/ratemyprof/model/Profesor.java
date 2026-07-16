package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "profesores")
@Data
@NoArgsConstructor
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Opcional: los horarios oficiales solo publican el apellido (a lo sumo una
     * inicial, ej: "Fernandez, V."), así que muchos profesores se cargan sin
     * nombre y se completa después desde la pantalla de admin. Vacío = no se sabe.
     */
    @Column(nullable = false)
    private String nombre = "";

    @NotBlank
    @Column(nullable = false)
    private String apellido;

    @OneToMany(mappedBy = "profesor")
    private List<Catedra> catedras;
}
