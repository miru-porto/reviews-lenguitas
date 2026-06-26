package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatedraService {

    private final CatedraRepository catedraRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Devuelve las cátedras de una materia, ordenadas por rating ascendente.
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
                .sorted(Comparator.comparingDouble(CatedraConRating::getPromedioRating))
                .toList();
    }

    public Catedra findById(Long id) {
        return catedraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cátedra no encontrada"));
    }
}
