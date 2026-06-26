package com.lenguas.ratemyprof.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // La raiz redirige a la pagina principal (lista de materias).
    @GetMapping("/")
    public String home() {
        return "redirect:/materias";
    }
}
