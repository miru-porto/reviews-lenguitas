package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Catedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CatedraRepository extends JpaRepository<Catedra, Long> {

    @Query("SELECT c FROM Catedra c JOIN FETCH c.profesor WHERE c.materia.id = :materiaId")
    List<Catedra> findByMateriaId(@Param("materiaId") Long materiaId);

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
}
