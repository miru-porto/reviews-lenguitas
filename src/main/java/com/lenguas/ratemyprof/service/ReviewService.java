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

    public List<ReviewView> findByCatedra(Long catedraId) {
        return reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(catedraId).stream()
                .map(r -> new ReviewView(
                        r.getUsuario().getNombre(),
                        r.getPuntuacion(),
                        r.getComentario(),
                        r.getFechaCreacion().format(FORMATO_FECHA)
                ))
                .toList();
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
