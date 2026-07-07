package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.exception.ConflictException;
import com.lenguas.ratemyprof.exception.ForbiddenException;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.model.VotoUtil;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import com.lenguas.ratemyprof.repository.VotoUtilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Valor del parámetro "orden" para ordenar por votos útiles. */
    public static final String ORDEN_UTILES = "utiles";

    private final ReviewRepository reviewRepository;
    private final CatedraRepository catedraRepository;
    private final VotoUtilRepository votoUtilRepository;

    /**
     * Reviews de una cátedra como view models. Por defecto ordenadas por fecha
     * (más recientes primero); con orden="utiles", por cantidad de votos útiles.
     */
    public List<ReviewView> findByCatedra(Long catedraId, String emailUsuarioActual, String orden) {
        // Votos por review en una sola query, para no hacer un COUNT por cada una.
        Map<Long, Long> votosPorReview = new HashMap<>();
        for (Object[] fila : votoUtilRepository.contarPorReviewDeCatedra(catedraId)) {
            votosPorReview.put((Long) fila[0], (Long) fila[1]);
        }
        Set<Long> misVotos = emailUsuarioActual == null
                ? Set.of()
                : Set.copyOf(votoUtilRepository.reviewIdsVotadasPor(emailUsuarioActual, catedraId));

        List<ReviewView> reviews = reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(catedraId).stream()
                .map(r -> new ReviewView(
                        r.getId(),
                        r.getUsuario().getNombre(),
                        r.getPuntuacion(),
                        r.getComentario(),
                        r.getFechaCreacion().format(FORMATO_FECHA),
                        emailUsuarioActual != null && emailUsuarioActual.equals(r.getUsuario().getEmail()),
                        votosPorReview.getOrDefault(r.getId(), 0L),
                        misVotos.contains(r.getId())
                ))
                .toList();

        if (ORDEN_UTILES.equals(orden)) {
            reviews = reviews.stream()
                    .sorted(Comparator.comparingLong(ReviewView::getVotosUtil).reversed())
                    .toList();
        }
        return reviews;
    }

    /**
     * Trae una review verificando que pertenezca al usuario. Si no existe o no es
     * suya, lanza excepción. Es la barrera de autorización del lado del servidor:
     * no alcanza con ocultar el botón en la vista.
     */
    public Review obtenerPropia(Long reviewId, Usuario usuario) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review no encontrada"));
        if (!review.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tenés permiso para modificar esta review");
        }
        return review;
    }

    /** Edita una review propia. Devuelve el id de la cátedra para redirigir. */
    public Long editar(Long reviewId, Usuario usuario, Integer puntuacion, String comentario) {
        Review review = obtenerPropia(reviewId, usuario);
        review.setPuntuacion(puntuacion);
        review.setComentario(comentario);
        reviewRepository.save(review);
        return review.getCatedra().getId();
    }

    /** Elimina una review propia. Devuelve el id de la cátedra para redirigir. */
    @Transactional
    public Long eliminar(Long reviewId, Usuario usuario) {
        Review review = obtenerPropia(reviewId, usuario);
        Long catedraId = review.getCatedra().getId();
        // Primero los votos que la referencian, si no la FK impide borrarla.
        votoUtilRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(review);
        return catedraId;
    }

    /**
     * Marca/desmarca una review como útil (toggle). No se puede votar la review
     * propia; el constraint único usuario+review respalda el anti-duplicado en la
     * base. Devuelve el id de la cátedra para redirigir.
     */
    @Transactional
    public Long votarUtil(Long reviewId, Usuario usuario) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review no encontrada"));
        if (review.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No podés votar tu propia review");
        }

        votoUtilRepository.findByUsuarioIdAndReviewId(usuario.getId(), reviewId)
                .ifPresentOrElse(
                        votoUtilRepository::delete,
                        () -> {
                            VotoUtil voto = new VotoUtil();
                            voto.setUsuario(usuario);
                            voto.setReview(review);
                            votoUtilRepository.save(voto);
                        });
        return review.getCatedra().getId();
    }

    public Review crear(Long catedraId, Usuario usuario, Integer puntuacion, String comentario) {
        // Validar que el usuario no haya hecho review de esta cátedra ya
        if (reviewRepository.existsByUsuarioIdAndCatedraId(usuario.getId(), catedraId)) {
            throw new ConflictException("Ya dejaste una review para esta cátedra");
        }

        Catedra catedra = catedraRepository.findById(catedraId)
                .orElseThrow(() -> new NotFoundException("Cátedra no encontrada"));

        Review review = new Review();
        review.setUsuario(usuario);
        review.setCatedra(catedra);
        review.setPuntuacion(puntuacion);
        review.setComentario(comentario);
        review.setFechaCreacion(LocalDateTime.now());

        return reviewRepository.save(review);
    }
}
