package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCatedraIdOrderByFechaCreacionDesc(Long catedraId);

    @Query("SELECT AVG(r.puntuacion) FROM Review r WHERE r.catedra.id = :catedraId")
    Double promedioByCatedraId(@Param("catedraId") Long catedraId);

    /**
     * Cuenta cuántas reviews hay por cada puntuación (1..5) en una cátedra.
     * Cada fila es un par [puntuacion (Integer), cantidad (Long)]. Las puntuaciones
     * sin ninguna review no aparecen en el resultado: hay que defaultearlas a 0.
     */
    @Query("SELECT r.puntuacion, COUNT(r) FROM Review r WHERE r.catedra.id = :catedraId GROUP BY r.puntuacion")
    List<Object[]> contarPorPuntuacion(@Param("catedraId") Long catedraId);

    boolean existsByUsuarioIdAndCatedraId(Long usuarioId, Long catedraId);
}
