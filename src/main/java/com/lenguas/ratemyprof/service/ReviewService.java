package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReviewRepository reviewRepository;
    private final CatedraRepository catedraRepository;

    public List<ReviewView> findByCatedra(Long catedraId, String emailUsuarioActual) {
        return reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(catedraId).stream()
                .map(r -> new ReviewView(
                        r.getId(),
                        r.getUsuario().getNombre(),
                        r.getPuntuacion(),
                        r.getComentario(),
                        r.getFechaCreacion().format(FORMATO_FECHA),
                        emailUsuarioActual != null && emailUsuarioActual.equals(r.getUsuario().getEmail())
                ))
                .toList();
    }

    /**
     * Trae una review verificando que pertenezca al usuario. Si no existe o no es
     * suya, lanza excepción. Es la barrera de autorización del lado del servidor:
     * no alcanza con ocultar el botón en la vista.
     */
    public Review obtenerPropia(Long reviewId, Usuario usuario) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));
        if (!review.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tenés permiso para modificar esta review");
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
    public Long eliminar(Long reviewId, Usuario usuario) {
        Review review = obtenerPropia(reviewId, usuario);
        Long catedraId = review.getCatedra().getId();
        reviewRepository.delete(review);
        return catedraId;
    }

    public Review crear(Long catedraId, Usuario usuario, Integer puntuacion, String comentario) {
        // Validar que el usuario no haya hecho review de esta cátedra ya
        if (reviewRepository.existsByUsuarioIdAndCatedraId(usuario.getId(), catedraId)) {
            throw new RuntimeException("Ya dejaste una review para esta cátedra");
        }

        Catedra catedra = catedraRepository.findById(catedraId)
                .orElseThrow(() -> new RuntimeException("Cátedra no encontrada"));

        Review review = new Review();
        review.setUsuario(usuario);
        review.setCatedra(catedra);
        review.setPuntuacion(puntuacion);
        review.setComentario(comentario);
        review.setFechaCreacion(LocalDateTime.now());

        return reviewRepository.save(review);
    }
}
