package com.lenguas.ratemyprof.repository;

import com.lenguas.ratemyprof.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCatedraIdOrderByFechaCreacionDesc(Long catedraId, Pageable pageable);

    /**
     * Reviews de la cátedra ordenadas por votos útiles (desempata por fecha).
     * El orden tiene que resolverse en la base: si se ordenara en memoria después
     * de paginar, cada página estaría bien ordenada pero el ranking global no.
     * El countQuery explícito evita que Spring intente derivarlo de una query
     * con subquery en el ORDER BY.
     */
    @Query(value = """
            SELECT r FROM Review r
            WHERE r.catedra.id = :catedraId
            ORDER BY (SELECT COUNT(v) FROM VotoUtil v WHERE v.review = r) DESC,
                     r.fechaCreacion DESC
            """,
            countQuery = "SELECT COUNT(r) FROM Review r WHERE r.catedra.id = :catedraId")
    Page<Review> findByCatedraIdOrdenVotosUtiles(@Param("catedraId") Long catedraId, Pageable pageable);

    /** ¿El usuario (por su id de Google) ya dejó review en esta cátedra? */
    boolean existsByUsuarioGoogleSubAndCatedraId(String googleSub, Long catedraId);

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

    /** ¿La cátedra tiene reviews? Bloquea el borrado de cátedras desde el admin. */
    boolean existsByCatedraId(Long catedraId);

    /** Todas las reviews de un usuario (ver UsuarioService.borrarCuenta). */
    @Modifying
    @Query("DELETE FROM Review r WHERE r.usuario.id = :usuarioId")
    void borrarReviewsDe(@Param("usuarioId") Long usuarioId);
}
