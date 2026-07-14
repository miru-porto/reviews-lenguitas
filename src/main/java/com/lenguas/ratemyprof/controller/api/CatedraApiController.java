package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.CatedraDetalle;
import com.lenguas.ratemyprof.dto.CatedraRequest;
import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.service.AdminService;
import com.lenguas.ratemyprof.service.CatedraService;
import com.lenguas.ratemyprof.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /** Tamaño de página por defecto y tope: nadie puede pedir 10.000 reviews de un saque. */
    static final int TAMANIO_PAGINA_DEFAULT = 5;
    static final int TAMANIO_PAGINA_MAX = 50;

    private final CatedraService catedraService;
    private final ReviewService reviewService;
    private final AdminService adminService;
    private final CatedraRepository catedraRepository;

    /**
     * Todas las cátedras, para la tabla del admin. Pública como el resto de la
     * lectura. La query trae profesor y materia con JOIN FETCH (sin N+1).
     */
    @GetMapping
    public List<CatedraView> listar() {
        return catedraRepository.findAllConProfesorYMateria().stream()
                .map(c -> new CatedraView(
                        c.getId(),
                        c.getMateria().getId(), c.getMateria().getNombre(),
                        c.getProfesor().getNombre(), c.getProfesor().getApellido()))
                .toList();
    }

    /** Encabezado + desglose de rating, para armar la página con un request. */
    @GetMapping("/{id}")
    public CatedraDetalle detalle(@PathVariable Long id, Authentication auth) {
        // findViewById lanza NotFoundException si la cátedra no existe.
        String dni = (auth != null) ? auth.getName() : null;
        return new CatedraDetalle(
                catedraService.findViewById(id),
                catedraService.desgloseRating(id),
                reviewService.yaReviewo(id, dni));
    }

    /**
     * Una página de reviews de la cátedra. Si hay sesión, esMia/laVoteUtil salen
     * calculados para ese usuario; sin sesión van en false.
     *
     * PagedModel serializa la Page con una forma estable:
     * { content: [...], page: { size, number, totalElements, totalPages } }.
     * (Serializar PageImpl directo funciona pero Spring lo desaconseja porque
     * su JSON no es un contrato garantizado entre versiones.)
     */
    @GetMapping("/{id}/reviews")
    public PagedModel<ReviewView> reviews(@PathVariable Long id,
                                          @RequestParam(defaultValue = "fecha") String orden,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "" + TAMANIO_PAGINA_DEFAULT) int size,
                                          Authentication auth) {
        catedraService.findViewById(id); // 404 si la cátedra no existe
        String dni = (auth != null) ? auth.getName() : null;
        // Valores fuera de rango se acotan en vez de responder 400: simplifica el cliente.
        PageRequest pagina = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANIO_PAGINA_MAX));
        return new PagedModel<>(reviewService.findByCatedra(id, dni, orden, pagina));
    }

    // -------------------- CRUD (ADMIN) --------------------

    /**
     * Crear una cátedra (par profesor+materia). 201; 404 si alguno de los dos
     * no existe, 409 si ese par ya tiene cátedra. No hay PUT: cambiar el par
     * es otra cátedra.
     */
    @PostMapping
    public ResponseEntity<CatedraView> crear(@Valid @RequestBody CatedraRequest req) {
        CatedraView creada = adminService.crearCatedra(req.getProfesorId(), req.getMateriaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** Borrar una cátedra sin reviews. 204; 409 si tiene reviews. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(@PathVariable Long id) {
        adminService.borrarCatedra(id);
        return ResponseEntity.noContent().build();
    }
}
