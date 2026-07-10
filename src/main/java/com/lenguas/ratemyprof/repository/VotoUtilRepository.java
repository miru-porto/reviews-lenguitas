package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.VotoUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VotoUtilRepository extends JpaRepository<VotoUtil, Long> {

    Optional<VotoUtil> findByUsuarioIdAndReviewId(Long usuarioId, Long reviewId);

    /** Borra todos los votos de una review (para poder borrar la review). */
    void deleteByReviewId(Long reviewId);

    /**
     * Cantidad de votos por review de una cátedra, en una sola query (evita
     * hacer un COUNT por review). Cada fila es [reviewId (Long), cantidad (Long)];
     * las reviews sin votos no aparecen: hay que defaultearlas a 0.
     */
    @Query("SELECT v.review.id, COUNT(v) FROM VotoUtil v WHERE v.review.catedra.id = :catedraId GROUP BY v.review.id")
    List<Object[]> contarPorReviewDeCatedra(@Param("catedraId") Long catedraId);

    /** Ids de las reviews de la cátedra que el usuario logueado ya votó. */
    @Query("SELECT v.review.id FROM VotoUtil v WHERE v.usuario.dni = :dni AND v.review.catedra.id = :catedraId")
    List<Long> reviewIdsVotadasPor(@Param("dni") String dni, @Param("catedraId") Long catedraId);
}
