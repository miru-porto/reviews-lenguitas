package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.MateriaView;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.service.CatedraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de materias. @RestController = @Controller + @ResponseBody: lo que
 * devuelve cada método no es el nombre de una template sino el objeto que
 * Jackson serializa a JSON en el cuerpo de la respuesta.
 */
@RestController
@RequestMapping("/api/materias")
@RequiredArgsConstructor
public class MateriaApiController {

    private final MateriaRepository materiaRepository;
    private final CatedraService catedraService;

    /** Todas las materias. Se mapea a DTO: la entidad expone relaciones LAZY. */
    @GetMapping
    public List<MateriaView> listar() {
        return materiaRepository.findAll().stream()
                .map(m -> new MateriaView(m.getId(), m.getNombre()))
                .toList();
    }

    /**
     * Cátedras de una materia con su rating, ordenadas de mayor a menor.
     * Valida que la materia exista: una lista vacía significa "materia sin
     * cátedras", no "materia inexistente" — eso es un 404.
     */
    @GetMapping("/{id}/catedras")
    public List<CatedraConRating> catedras(@PathVariable Long id) {
        if (!materiaRepository.existsById(id)) {
            throw new NotFoundException("Materia no encontrada");
        }
        return catedraService.findByMateriaOrdenadoPorRating(id);
    }
}
