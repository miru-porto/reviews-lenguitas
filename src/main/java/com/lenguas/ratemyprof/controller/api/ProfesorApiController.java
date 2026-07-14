package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.ProfesorRequest;
import com.lenguas.ratemyprof.dto.ProfesorView;
import com.lenguas.ratemyprof.repository.ProfesorRepository;
import com.lenguas.ratemyprof.service.AdminService;
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
 * API de profesores. El GET es público (lo usa el form de cátedras del admin,
 * pero no expone nada sensible); POST/PUT/DELETE piden rol ADMIN vía la
 * filter chain de SecurityConfig.
 */
@RestController
@RequestMapping("/api/profesores")
@RequiredArgsConstructor
public class ProfesorApiController {

    private final ProfesorRepository profesorRepository;
    private final AdminService adminService;

    /** Todos los profesores, ordenados por apellido. */
    @GetMapping
    public List<ProfesorView> listar() {
        return profesorRepository.findAll(Sort.by("apellido", "nombre")).stream()
                .map(ProfesorView::de)
                .toList();
    }

    /** Alta de profesor. 201 con la vista. */
    @PostMapping
    public ResponseEntity<ProfesorView> crear(@Valid @RequestBody ProfesorRequest req) {
        ProfesorView creado = adminService.crearProfesor(req.getNombre(), req.getApellido());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** Editar nombre/apellido. 204; 404 si no existe. */
    @PutMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id,
                                       @Valid @RequestBody ProfesorRequest req) {
        adminService.editarProfesor(id, req.getNombre(), req.getApellido());
        return ResponseEntity.noContent().build();
    }

    /** Borrar un profesor sin cátedras. 204; 409 si tiene cátedras. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        adminService.borrarProfesor(id);
        return ResponseEntity.noContent().build();
    }
}
