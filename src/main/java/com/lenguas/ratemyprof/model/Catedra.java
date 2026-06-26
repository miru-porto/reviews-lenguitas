package com.lenguas.ratemyprof.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "catedras", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"profesor_id", "materia_id"})
})
@Data
@NoArgsConstructor
public class Catedra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @OneToMany(mappedBy = "catedra")
    private List<Review> reviews;
}
