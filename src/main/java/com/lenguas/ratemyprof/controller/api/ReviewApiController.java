package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.CrearReviewRequest;
import com.lenguas.ratemyprof.dto.ReviewCreadaResponse;
import com.lenguas.ratemyprof.dto.ReviewForm;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.ReviewService;
import com.lenguas.ratemyprof.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * API de escritura de reviews: verbos REST de verdad (POST/PUT/DELETE) en vez
 * de los /review/nueva, /editar, /borrar del form Thymeleaf. Todas requieren
 * sesión (lo garantiza la filter chain de /api); el usuario sale de Authentication.
 *
 * La lógica (autorización, duplicados, borrado de votos) vive en ReviewService,
 * el mismo que usa el controller Thymeleaf: acá solo traducimos a HTTP.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewApiController {

    private final ReviewService reviewService;
    private final UsuarioService usuarioService;

    /** Crear una review. 201 con el id nuevo y un header Location. */
    @PostMapping
    public ResponseEntity<ReviewCreadaResponse> crear(@Valid @RequestBody CrearReviewRequest req,
                                                      Authentication auth) {
        Usuario usuario = usuarioService.findByDni(auth.getName());
        Review review = reviewService.crear(
                req.getCatedraId(), usuario, req.getPuntuacion(), req.getComentario());
        ReviewCreadaResponse body = new ReviewCreadaResponse(review.getId(), req.getCatedraId());
        return ResponseEntity.created(URI.create("/api/reviews/" + review.getId())).body(body);
    }

    /** Editar una review propia. 204 sin cuerpo; el service verifica el dueño. */
    @PutMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id,
                                       @Valid @RequestBody ReviewForm form,
                                       Authentication auth) {
        Usuario usuario = usuarioService.findByDni(auth.getName());
        reviewService.editar(id, usuario, form.getPuntuacion(), form.getComentario());
        return ResponseEntity.noContent().build();
    }

    /** Borrar una review propia. 204; el service verifica el dueño. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication auth) {
        Usuario usuario = usuarioService.findByDni(auth.getName());
        reviewService.eliminar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marcar/desmarcar una review como útil (toggle). 204: React vuelve a pedir
     * la lista de reviews para ver el conteo actualizado.
     */
    @PostMapping("/{id}/util")
    public ResponseEntity<Void> votarUtil(@PathVariable Long id, Authentication auth) {
        Usuario usuario = usuarioService.findByDni(auth.getName());
        reviewService.votarUtil(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
