package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.NivelRating;
import com.lenguas.ratemyprof.dto.RatingBreakdown;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatedraService {

    private final CatedraRepository catedraRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Devuelve las cátedras de una materia, ordenadas por rating descendente.
     * Este es el filtro principal que pidió la usuaria.
     */
    public List<CatedraConRating> findByMateriaOrdenadoPorRating(Long materiaId) {
        List<Catedra> catedras = catedraRepository.findByMateriaId(materiaId);

        return catedras.stream()
                .map(c -> {
                    Double promedio = reviewRepository.promedioByCredatraId(c.getId());
                    long cantidad = reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(c.getId()).size();
                    return new CatedraConRating(
                            c.getId(),
                            c.getProfesor().getNombre(),
                            c.getProfesor().getApellido(),
                            c.getMateria().getNombre(),
                            promedio != null ? promedio : 0.0,
                            cantidad
                    );
                })
                .sorted(Comparator.comparingDouble(CatedraConRating::getPromedioRating).reversed())
                .toList();
    }

    public Catedra findById(Long id) {
        return catedraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cátedra no encontrada"));
    }

    /**
     * Versión de salida (view model) para la página de reviews: aplana profesor y
     * materia para que la vista no toque la entidad JPA ni sus relaciones LAZY.
     */
    public CatedraView findViewById(Long id) {
        Catedra c = findById(id);
        return new CatedraView(
                c.getId(),
                c.getMateria().getId(),
                c.getMateria().getNombre(),
                c.getProfesor().getNombre(),
                c.getProfesor().getApellido()
        );
    }

    /**
     * Desglose de rating de una cátedra: promedio, total y distribución por
     * cantidad de estrellas (5 a 1), con 0 en los niveles que no tienen reviews.
     */
    public RatingBreakdown desgloseRating(Long catedraId) {
        Map<Integer, Long> conteos = new HashMap<>();
        for (Object[] fila : reviewRepository.contarPorPuntuacion(catedraId)) {
            conteos.put((Integer) fila[0], (Long) fila[1]);
        }

        long total = conteos.values().stream().mapToLong(Long::longValue).sum();

        List<NivelRating> niveles = new ArrayList<>();
        for (int estrellas = 5; estrellas >= 1; estrellas--) {
            long cantidad = conteos.getOrDefault(estrellas, 0L);
            int porcentaje = total == 0 ? 0 : (int) Math.round(cantidad * 100.0 / total);
            niveles.add(new NivelRating(estrellas, cantidad, porcentaje));
        }

        Double promedio = reviewRepository.promedioByCredatraId(catedraId);
        return new RatingBreakdown(promedio != null ? promedio : 0.0, total, niveles);
    }
}
