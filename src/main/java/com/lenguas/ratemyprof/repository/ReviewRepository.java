package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCatedraIdOrderByFechaCreacionDesc(Long catedraId);

    @Query("SELECT AVG(r.puntuacion) FROM Review r WHERE r.catedra.id = :catedraId")
    Double promedioByCredatraId(@Param("catedraId") Long catedraId);

    boolean existsByUsuarioIdAndCatedraId(Long usuarioId, Long catedraId);
}
