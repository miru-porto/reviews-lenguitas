package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.MateriaView;
import com.lenguas.ratemyprof.dto.ResultadosBusqueda;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusquedaService {

    private final MateriaRepository materiaRepository;
    private final CatedraRepository catedraRepository;

    /**
     * Busca materias y profesores por nombre parcial con un único término.
     * Devuelve listas vacías si el término está en blanco (no busca "todo").
     */
    public ResultadosBusqueda buscar(String consulta) {
        String q = consulta == null ? "" : consulta.trim();
        if (q.isEmpty()) {
            return new ResultadosBusqueda(q, List.of(), List.of());
        }

        List<MateriaView> materias = materiaRepository.findByNombreContainingIgnoreCase(q).stream()
                .map(m -> new MateriaView(m.getId(), m.getNombre(), m.getAnio()))
                .toList();

        List<CatedraView> catedras = catedraRepository.buscarPorProfesor(q).stream()
                .map(c -> new CatedraView(
                        c.getId(),
                        c.getMateria().getId(),
                        c.getMateria().getNombre(),
                        c.getProfesor().getNombre(),
                        c.getProfesor().getApellido()
                ))
                .toList();

        return new ResultadosBusqueda(q, materias, catedras);
    }
}
