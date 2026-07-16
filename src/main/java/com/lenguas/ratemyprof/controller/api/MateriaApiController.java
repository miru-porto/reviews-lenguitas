package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.MateriaRequest;
import com.lenguas.ratemyprof.dto.MateriaView;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.service.AdminService;
import com.lenguas.ratemyprof.service.CatedraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API de materias. @RestController = @Controller + @ResponseBody: lo que
 * devuelve cada método no es el nombre de una template sino el objeto que
 * Jackson serializa a JSON en el cuerpo de la respuesta.
 *
 * Los GET son públicos; POST/PUT/DELETE piden rol ADMIN (regla en
 * SecurityConfig, no acá: la autorización por rol es transversal y vive
 * en la filter chain).
 */
@RestController
@RequestMapping("/api/materias")
@RequiredArgsConstructor
public class MateriaApiController {

    private final MateriaRepository materiaRepository;
    private final CatedraService catedraService;
    private final AdminService adminService;

    /**
     * Todas las materias, ordenadas por año de cursada y nombre (las sin año
     * quedan al final). Se mapea a DTO: la entidad expone relaciones LAZY.
     */
    @GetMapping
    public List<MateriaView> listar() {
        return materiaRepository.findAll(Sort.by("anio", "nombre")).stream()
                .map(m -> new MateriaView(m.getId(), m.getNombre(), m.getAnio()))
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

    // -------------------- CRUD (ADMIN) --------------------

    /** Crear una materia. 201 con la vista; 409 si el nombre ya existe. */
    @PostMapping
    public ResponseEntity<MateriaView> crear(@Valid @RequestBody MateriaRequest req) {
        MateriaView creada = adminService.crearMateria(req.getNombre(), req.getAnio());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** Renombrar una materia. 204; 404 si no existe, 409 si el nombre choca. */
    @PutMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id,
                                       @Valid @RequestBody MateriaRequest req) {
        adminService.editarMateria(id, req.getNombre(), req.getAnio());
        return ResponseEntity.noContent().build();
    }

    /** Borrar una materia sin cátedras. 204; 409 si tiene cátedras. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        adminService.borrarMateria(id);
        return ResponseEntity.noContent().build();
    }
}
