package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {

    /** Búsqueda por nombre parcial, sin distinguir mayúsculas (LIKE %q%). */
    List<Materia> findByNombreContainingIgnoreCase(String nombre);

    /** ¿Ya existe una materia con este nombre? (alta desde el admin) */
    boolean existsByNombreIgnoreCase(String nombre);

    /** Como el anterior pero excluyendo una materia (para editarla sin chocar consigo misma). */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
