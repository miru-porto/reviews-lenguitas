package com.lenguas.ratemyprof.controller;

import com.lenguas.ratemyprof.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    /**
     * Búsqueda por nombre de materia o de profesor (público).
     * GET con query param para que la URL sea compartible: /buscar?q=fonetica
     */
    @GetMapping("/buscar")
    public String buscar(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("resultados", busquedaService.buscar(q));
        return "resultados-busqueda";
    }
}
