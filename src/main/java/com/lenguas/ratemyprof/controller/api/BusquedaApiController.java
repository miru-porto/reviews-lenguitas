package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.dto.ResultadosBusqueda;
import com.lenguas.ratemyprof.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de búsqueda: GET /api/buscar?q=... — mismo service que la página MVC;
 * con q vacío devuelve listas vacías, igual que allá.
 */
@RestController
@RequiredArgsConstructor
public class BusquedaApiController {

    private final BusquedaService busquedaService;

    @GetMapping("/api/buscar")
    public ResultadosBusqueda buscar(@RequestParam(defaultValue = "") String q) {
        return busquedaService.buscar(q);
    }
}
