package com.lenguas.ratemyprof.controller;

import com.lenguas.ratemyprof.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    // Alta desde las páginas Thymeleaf. El modelo de auth pasó a ser por DNI
    // (ver AuthApiController); estas páginas se retiran en la Fase 6, así que
    // acá solo mantenemos la compilación contra la firma nueva del service.
    @PostMapping("/registro")
    public String registrar(@RequestParam String dni,
                            @RequestParam String nombre,
                            Model model) {
        try {
            usuarioService.registrar(dni, nombre);
            return "redirect:/login?registrado";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}
