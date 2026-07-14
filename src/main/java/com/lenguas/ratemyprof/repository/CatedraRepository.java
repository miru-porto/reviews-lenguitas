package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.CatedraConRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CatedraRepository extends JpaRepository<Catedra, Long> {

    /**
     * Cátedras de una materia con su rating, en UNA sola query: el LEFT JOIN
     * mantiene las cátedras sin reviews (promedio 0), y el GROUP BY calcula
     * promedio y cantidad por cátedra. El "new ..." (constructor expression)
     * hace que JPA instancie el DTO directamente desde el SELECT.
     */
    @Query("""
            SELECT new com.lenguas.ratemyprof.model.CatedraConRating(
                c.id, p.nombre, p.apellido, m.nombre,
                COALESCE(AVG(r.puntuacion), 0.0), COUNT(r))
            FROM Catedra c
            JOIN c.profesor p
            JOIN c.materia m
            LEFT JOIN c.reviews r
            WHERE m.id = :materiaId
            GROUP BY c.id, p.nombre, p.apellido, m.nombre
            ORDER BY COALESCE(AVG(r.puntuacion), 0.0) DESC
            """)
    List<CatedraConRating> findByMateriaConRating(@Param("materiaId") Long materiaId);

    /**
     * Cátedras cuyo profesor coincide (parcial, sin mayúsculas) por nombre o
     * apellido. Devuelve cátedras y no profesores porque la página destino de
     * un resultado es la de la cátedra (/catedra/{id}).
     */
    @Query("""
            SELECT c FROM Catedra c
            JOIN FETCH c.profesor p
            JOIN FETCH c.materia
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Catedra> buscarPorProfesor(@Param("q") String q);

    /**
     * Todas las cátedras con profesor y materia ya cargados (JOIN FETCH), para
     * la tabla del admin. Sin el fetch, serializar la lista dispararía una
     * query por cátedra (N+1) al tocar las relaciones LAZY.
     */
    @Query("SELECT c FROM Catedra c JOIN FETCH c.profesor JOIN FETCH c.materia ORDER BY c.materia.nombre, c.profesor.apellido")
    List<Catedra> findAllConProfesorYMateria();

    // Chequeos de integridad para el CRUD del admin.
    boolean existsByProfesorIdAndMateriaId(Long profesorId, Long materiaId);
    boolean existsByMateriaId(Long materiaId);
    boolean existsByProfesorId(Long profesorId);
}
