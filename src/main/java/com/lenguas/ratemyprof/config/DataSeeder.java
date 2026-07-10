package com.lenguas.ratemyprof.config;

import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Materia;
import com.lenguas.ratemyprof.model.Profesor;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.repository.ProfesorRepository;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos de ejemplo la primera vez que arranca la app contra una base vacia.
 * Es idempotente: si ya hay materias cargadas, no hace nada.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final MateriaRepository materiaRepository;
    private final ProfesorRepository profesorRepository;
    private final CatedraRepository catedraRepository;
    private final UsuarioRepository usuarioRepository;

    public DataSeeder(MateriaRepository materiaRepository,
                      ProfesorRepository profesorRepository,
                      CatedraRepository catedraRepository,
                      UsuarioRepository usuarioRepository) {
        this.materiaRepository = materiaRepository;
        this.profesorRepository = profesorRepository;
        this.catedraRepository = catedraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        // Si ya hay datos, no sembramos de nuevo.
        if (materiaRepository.count() > 0) {
            return;
        }

        // ===== Materias =====
        List<Materia> materias = new ArrayList<>();
        for (String nombre : List.of(
                "Fonética Inglesa I",
                "Gramática Inglesa I",
                "Literatura Inglesa",
                "Práctica de la Pronunciación",
                "Lengua Francesa I")) {
            Materia m = new Materia();
            m.setNombre(nombre);
            materias.add(m);
        }
        materiaRepository.saveAll(materias);

        // ===== Profesores =====
        String[][] profes = {
                {"María", "González"},
                {"Carlos", "Fernández"},
                {"Ana", "Martínez"},
                {"Laura", "Rodríguez"},
                {"Pablo", "López"}
        };
        List<Profesor> profesores = new ArrayList<>();
        for (String[] p : profes) {
            Profesor prof = new Profesor();
            prof.setNombre(p[0]);
            prof.setApellido(p[1]);
            profesores.add(prof);
        }
        profesorRepository.saveAll(profesores);

        // ===== Cátedras (indices 0-based: profesor, materia) =====
        int[][] pares = {
                {0, 0}, // González - Fonética Inglesa I
                {1, 0}, // Fernández - Fonética Inglesa I
                {2, 1}, // Martínez - Gramática Inglesa I
                {3, 1}, // Rodríguez - Gramática Inglesa I
                {0, 2}, // González - Literatura Inglesa
                {4, 3}, // López - Práctica de la Pronunciación
                {2, 4}  // Martínez - Lengua Francesa I
        };
        List<Catedra> catedras = new ArrayList<>();
        for (int[] par : pares) {
            Catedra c = new Catedra();
            c.setProfesor(profesores.get(par[0]));
            c.setMateria(materias.get(par[1]));
            catedras.add(c);
        }
        catedraRepository.saveAll(catedras);

        // ===== Usuario de prueba (login con DNI "12345678") =====
        Usuario usuario = new Usuario();
        usuario.setNombre("Estudiante Test");
        usuario.setDni("12345678");
        usuarioRepository.save(usuario);
    }
}
