package com.lenguas.ratemyprof.controller;

import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.ReviewForm;
import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.CatedraService;
import com.lenguas.ratemyprof.service.ReviewService;
import com.lenguas.ratemyprof.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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
        CatedraView catedra = catedraService.findViewById(id);
        List<ReviewView> reviews = reviewService.findByCatedra(id);
        model.addAttribute("catedra", catedra);
        model.addAttribute("reviews", reviews);
        model.addAttribute("desglose", catedraService.desgloseRating(id));
        return "reviews";
    }

    /**
     * Formulario para dejar una review (requiere login).
     */
    @GetMapping("/review/nueva/{catedraId}")
    public String formularioReview(@PathVariable Long catedraId, Model model) {
        Catedra catedra = catedraService.findById(catedraId);
        model.addAttribute("catedra", catedra);
        model.addAttribute("reviewForm", new ReviewForm());
        return "nueva-review";
    }

    /**
     * Procesar la review enviada.
     */
    @PostMapping("/review/nueva/{catedraId}")
    public String crearReview(@PathVariable Long catedraId,
                              @Valid @ModelAttribute("reviewForm") ReviewForm form,
                              BindingResult result,
                              Authentication auth,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("catedra", catedraService.findById(catedraId));
            return "nueva-review";
        }
        try {
            Usuario usuario = usuarioService.findByEmail(auth.getName());
            reviewService.crear(catedraId, usuario, form.getPuntuacion(), form.getComentario());
            return "redirect:/catedra/" + catedraId + "?exito";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("catedra", catedraService.findById(catedraId));
            return "nueva-review";
        }
    }
}
