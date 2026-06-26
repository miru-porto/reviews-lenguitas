package com.lenguas.ratemyprof.controller;

import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.CatedraService;
import com.lenguas.ratemyprof.service.ReviewService;
import com.lenguas.ratemyprof.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CatedraService catedraService;
    private final UsuarioService usuarioService;

    /**
     * Ver todas las reviews de una cátedra (público).
     */
    @GetMapping("/catedra/{id}")
    public String verReviews(@PathVariable Long id, Model model) {
        Catedra catedra = catedraService.findById(id);
        List<Review> reviews = reviewService.findByCatedra(id);
        model.addAttribute("catedra", catedra);
        model.addAttribute("reviews", reviews);
        return "reviews";
    }

    /**
     * Formulario para dejar una review (requiere login).
     */
    @GetMapping("/review/nueva/{catedraId}")
    public String formularioReview(@PathVariable Long catedraId, Model model) {
        Catedra catedra = catedraService.findById(catedraId);
        model.addAttribute("catedra", catedra);
        return "nueva-review";
    }

    /**
     * Procesar la review enviada.
     */
    @PostMapping("/review/nueva/{catedraId}")
    public String crearReview(@PathVariable Long catedraId,
                              @RequestParam Integer puntuacion,
                              @RequestParam String comentario,
                              Authentication auth,
                              Model model) {
        try {
            Usuario usuario = usuarioService.findByEmail(auth.getName());
            reviewService.crear(catedraId, usuario, puntuacion, comentario);
            return "redirect:/catedra/" + catedraId + "?exito";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("catedra", catedraService.findById(catedraId));
            return "nueva-review";
        }
    }
}
