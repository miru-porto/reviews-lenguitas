package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.MateriaView;
import com.lenguas.ratemyprof.dto.ProfesorView;
import com.lenguas.ratemyprof.exception.ConflictException;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Materia;
import com.lenguas.ratemyprof.model.Profesor;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.repository.ProfesorRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * CRUD del catálogo (materias, profesores, cátedras) para el rol ADMIN.
 * La restricción de rol vive en SecurityConfig (hasRole sobre /api/...);
 * acá va la lógica de negocio: unicidad (409) y protecciones de borrado.
 *
 * Regla de borrado: nada se borra en cascada. Una materia/profesor con
 * cátedras, o una cátedra con reviews, devuelve 409 — mejor que el admin
 * borre explícitamente de abajo hacia arriba a que un click destruya
 * reviews de usuarios sin querer.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final MateriaRepository materiaRepository;
    private final ProfesorRepository profesorRepository;
    private final CatedraRepository catedraRepository;
    private final ReviewRepository reviewRepository;

    // -------------------- Materias --------------------

    public MateriaView crearMateria(String nombre, Integer anio) {
        String limpio = nombre.trim();
        if (materiaRepository.existsByNombreIgnoreCase(limpio)) {
            throw new ConflictException("Ya existe una materia con ese nombre");
        }
        Materia materia = new Materia();
        materia.setNombre(limpio);
        materia.setAnio(anio);
        materia = materiaRepository.save(materia);
        return new MateriaView(materia.getId(), materia.getNombre(), materia.getAnio());
    }

    public void editarMateria(Long id, String nombre, Integer anio) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Materia no encontrada"));
        String limpio = nombre.trim();
        // IdNot: sin excluirse a sí misma, guardar sin cambiar el nombre daría 409.
        if (materiaRepository.existsByNombreIgnoreCaseAndIdNot(limpio, id)) {
            throw new ConflictException("Ya existe una materia con ese nombre");
        }
        materia.setNombre(limpio);
        materia.setAnio(anio);
        materiaRepository.save(materia);
    }

    public void borrarMateria(Long id) {
        if (!materiaRepository.existsById(id)) {
            throw new NotFoundException("Materia no encontrada");
        }
        if (catedraRepository.existsByMateriaId(id)) {
            throw new ConflictException("La materia tiene cátedras asociadas: borrálas primero");
        }
        materiaRepository.deleteById(id);
    }

    // -------------------- Profesores --------------------

    public ProfesorView crearProfesor(String nombre, String apellido) {
        // Sin chequeo de duplicados: dos profesores pueden llamarse igual.
        Profesor profesor = new Profesor();
        // El nombre es opcional (ver Profesor): null se normaliza a vacío.
        profesor.setNombre(nombre == null ? "" : nombre.trim());
        profesor.setApellido(apellido.trim());
        return ProfesorView.de(profesorRepository.save(profesor));
    }

    public void editarProfesor(Long id, String nombre, String apellido) {
        Profesor profesor = profesorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profesor no encontrado"));
        profesor.setNombre(nombre == null ? "" : nombre.trim());
        profesor.setApellido(apellido.trim());
        profesorRepository.save(profesor);
    }

    public void borrarProfesor(Long id) {
        if (!profesorRepository.existsById(id)) {
            throw new NotFoundException("Profesor no encontrado");
        }
        if (catedraRepository.existsByProfesorId(id)) {
            throw new ConflictException("El profesor tiene cátedras asociadas: borrálas primero");
        }
        profesorRepository.deleteById(id);
    }

    // -------------------- Cátedras --------------------

    public CatedraView crearCatedra(Long profesorId, Long materiaId) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new NotFoundException("Profesor no encontrado"));
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NotFoundException("Materia no encontrada"));
        // El par (profesor, materia) es único; se valida acá para responder un
        // 409 con mensaje claro en vez de dejar explotar la constraint (500).
        if (catedraRepository.existsByProfesorIdAndMateriaId(profesorId, materiaId)) {
            throw new ConflictException("Ese profesor ya tiene una cátedra en esa materia");
        }
        Catedra catedra = new Catedra();
        catedra.setProfesor(profesor);
        catedra.setMateria(materia);
        catedra = catedraRepository.save(catedra);
        return new CatedraView(
                catedra.getId(),
                materia.getId(), materia.getNombre(),
                profesor.getNombre(), profesor.getApellido());
    }

    public void borrarCatedra(Long id) {
        if (!catedraRepository.existsById(id)) {
            throw new NotFoundException("Cátedra no encontrada");
        }
        if (reviewRepository.existsByCatedraId(id)) {
            throw new ConflictException("La cátedra tiene reviews: no se puede borrar");
        }
        catedraRepository.deleteById(id);
    }
}
