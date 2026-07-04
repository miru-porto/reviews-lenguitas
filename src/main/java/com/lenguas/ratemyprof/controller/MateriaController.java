package com.lenguas.ratemyprof.controller;

import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.service.CatedraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaRepository materiaRepository;
    private final CatedraService catedraService;

    /**
     * Lista todas las materias disponibles.
     */
    @GetMapping("/materias")
    public String listarMaterias(Model model) {
        model.addAttribute("materias", materiaRepository.findAll());
        return "materias";
    }

    /**
     * Filtro por materia: muestra las cátedras ordenadas por rating descendente.
     */
    @GetMapping("/materias/{id}")
    public String verCatedrasPorMateria(@PathVariable Long id, Model model) {
        List<CatedraConRating> catedras = catedraService.findByMateriaOrdenadoPorRating(id);
        model.addAttribute("catedras", catedras);
        model.addAttribute("materiaNombre", materiaRepository.findById(id).orElseThrow().getNombre());
        return "catedras-por-materia";
    }
}
