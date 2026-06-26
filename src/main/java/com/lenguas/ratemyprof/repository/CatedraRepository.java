package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Catedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CatedraRepository extends JpaRepository<Catedra, Long> {

    @Query("SELECT c FROM Catedra c JOIN FETCH c.profesor WHERE c.materia.id = :materiaId")
    List<Catedra> findByMateriaId(@Param("materiaId") Long materiaId);
}
