package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.CatedraDetalle;
import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.service.CatedraService;
import com.lenguas.ratemyprof.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de cátedras y sus reviews. Ruta en plural (/api/catedras) siguiendo la
 * convención REST, aunque la página MVC use /catedra/{id} en singular.
 */
@RestController
@RequestMapping("/api/catedras")
@RequiredArgsConstructor
public class CatedraApiController {

    private final CatedraService catedraService;
    private final ReviewService reviewService;

    /** Encabezado + desglose de rating, para armar la página con un request. */
    @GetMapping("/{id}")
    public CatedraDetalle detalle(@PathVariable Long id) {
        // findViewById lanza NotFoundException si la cátedra no existe.
        return new CatedraDetalle(
                catedraService.findViewById(id),
                catedraService.desgloseRating(id));
    }

    /**
     * Reviews de la cátedra. Si hay sesión (por ahora, la del sitio Thymeleaf),
     * esMia/laVoteUtil salen calculados para ese usuario; sin sesión van en false.
     */
    @GetMapping("/{id}/reviews")
    public List<ReviewView> reviews(@PathVariable Long id,
                                    @RequestParam(defaultValue = "fecha") String orden,
                                    Authentication auth) {
        catedraService.findViewById(id); // 404 si la cátedra no existe
        String email = (auth != null) ? auth.getName() : null;
        return reviewService.findByCatedra(id, email, orden);
    }
}
