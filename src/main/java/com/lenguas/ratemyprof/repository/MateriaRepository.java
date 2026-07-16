package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Materia;
import com.lenguas.ratemyprof.model.MateriaConStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    /**
     * Todas las materias con sus agregados para la portada, en UNA query (si
     * no, serían dos por materia: N+1 con el catálogo entero). Los LEFT JOIN
     * mantienen las materias sin cátedras y las cátedras sin reviews.
     *
     * COUNT(DISTINCT c) es obligatorio: el join con reviews repite la fila de
     * la cátedra una vez por review, así que un COUNT(c) pelado contaría
     * reviews disfrazadas de cátedras. COUNT(r) no necesita DISTINCT porque
     * cada review aparece exactamente una vez.
     *
     * El ORDER BY replica el Sort.by("anio", "nombre") que usaba el listado:
     * en Postgres los NULL van al final por defecto, o sea las materias sin
     * año quedan últimas, que es lo que la portada espera.
     */
    @Query("""
            SELECT new com.lenguas.ratemyprof.model.MateriaConStats(
                m.id, m.nombre, m.anio,
                COALESCE(AVG(r.puntuacion), 0.0), COUNT(DISTINCT c), COUNT(r))
            FROM Materia m
            LEFT JOIN m.catedras c
            LEFT JOIN c.reviews r
            GROUP BY m.id, m.nombre, m.anio
            ORDER BY m.anio, m.nombre
            """)
    List<MateriaConStats> findAllConStats();

    /** Búsqueda por nombre parcial, sin distinguir mayúsculas (LIKE %q%). */
    List<Materia> findByNombreContainingIgnoreCase(String nombre);

    /** ¿Ya existe una materia con este nombre? (alta desde el admin) */
    boolean existsByNombreIgnoreCase(String nombre);

    /** Como el anterior pero excluyendo una materia (para editarla sin chocar consigo misma). */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
