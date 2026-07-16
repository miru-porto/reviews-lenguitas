package com.lenguas.ratemyprof.config;

import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Materia;
import com.lenguas.ratemyprof.model.Profesor;
import com.lenguas.ratemyprof.model.Rol;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.repository.ProfesorRepository;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Carga el catálogo real del Profesorado de Inglés la primera vez que arranca
 * la app contra una base vacía. Es idempotente: si ya hay materias, no hace nada.
 *
 * Fuentes de los datos (PDFs en la raíz del repo):
 *  - Materias y su año de cursada: "materias y sistema de correlativas.pdf"
 *    (solo la carrera de Inglés; las variantes de Portugués quedan afuera).
 *  - Cátedras (qué profesor dicta cada materia): horarios de los turnos mañana
 *    y vespertino del 1er y 2do cuatrimestre 2026. De ahí solo salen apellidos
 *    (a lo sumo una inicial), por eso el nombre de los profesores queda vacío.
 *
 * El mismo catálogo está en data-seed.sql (versión SQL, base de la futura
 * migración Flyway): si se cambia acá hay que cambiarlo allá también.
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

    /** Materias por año de cursada: {anio, nombre}. */
    private static final Object[][] MATERIAS = {
            // ----- 1er año -----
            {1, "Lengua Inglesa 1"},
            {1, "Gramática Inglesa 1"},
            {1, "Fonología y Práctica de Laboratorio 1"},
            {1, "Taller 1"},
            {1, "Taller 2"},
            {1, "Pedagogía"},
            {1, "Didáctica General"},
            {1, "Psicología Educacional"},
            {1, "Taller de Nuevas Tecnologías"},
            {1, "Taller de Lectura, Escritura y Oralidad (TLEO)"},
            // ----- 2do año -----
            {2, "Lengua Inglesa 2"},
            {2, "Gramática Inglesa 2"},
            {2, "Fonología y Práctica de Laboratorio 2"},
            {2, "Cultura de los Pueblos de Habla Inglesa 1"},
            {2, "Didáctica Específica 1"},
            {2, "Didáctica Específica 2"},
            {2, "Creatividad 1"},
            {2, "Taller 3"},
            {2, "Taller 4"},
            {2, "Sujetos de la Educación 1"},
            {2, "Sistema y Política Educativa"},
            {2, "Metodología de la Investigación"},
            {2, "Educación Sexual Integral (ESI)"},
            // ----- 3er año -----
            {3, "Lengua Inglesa 3"},
            {3, "Lingüística"},
            {3, "Fonología y Práctica de Laboratorio 3"},
            {3, "Literatura en Lengua Inglesa 1"},
            {3, "Literatura en Lengua Inglesa 2"},
            {3, "Sujetos de la Educación 2"},
            {3, "Creatividad 2"},
            {3, "Taller 5 (de Inicial y Primaria)"},
            {3, "Residencia para el Nivel Inicial y Primario"},
            {3, "Seminario de Investigación Acción 1"},
            {3, "Informática para la Enseñanza"},
            {3, "Instituciones Educativas"},
            {3, "Saberes Lúdicos, Corporales y Motores"},
            {3, "Trabajo de Campo"},
            // ----- 4to año -----
            {4, "Lengua Inglesa 4"},
            {4, "Cultura de los Pueblos de Habla Inglesa 2"},
            {4, "Literatura en Lengua Inglesa 3"},
            {4, "Análisis y Redacción de Textos"},
            {4, "Teatro"},
            {4, "Taller 6 (de Nivel Medio)"},
            {4, "Residencia para el Nivel Medio"},
            {4, "Seminario de Investigación Acción 2"},
            {4, "Nuevos Escenarios, Cultura, Tecnología y Subjetividad"},
            {4, "Trabajo / Profesionalización Docente"},
            {4, "Filosofía"},
            {4, "TIC Aplicadas"},
            // ----- 5to año (solo plan de 5 años) -----
            {5, "Portugués Ab Initio 1"},
            {5, "Portugués Ab Initio 2"},
            {5, "Cultura de los Pueblos de Habla Inglesa 3"},
            {5, "Literatura en Lengua Inglesa 4"},
            {5, "Residencia para el Nivel Superior"},
            {5, "Taller de Música"},
    };

    /**
     * Profesores: {nombre, apellido}. El nombre es la inicial cuando el horario
     * la publica (sirve para distinguir "Costa" de "Costa, F."), si no, vacío.
     */
    private static final String[][] PROFESORES = {
            {"", "De Domenico"}, {"", "Ragno"}, {"", "Crottogini"}, {"", "Costa"},
            {"F.", "Costa"}, {"", "Mortoro"}, {"", "Rossell"}, {"V.", "Fernandez"},
            {"", "Rodríguez"}, {"", "Caligaris"}, {"", "Pérez Ponsa"}, {"", "Massolo"},
            {"", "Carteau"}, {"", "Morelli"}, {"", "Bessega"}, {"", "Castino"},
            {"", "Jaschek"}, {"", "Del Regno"}, {"", "Bergel"}, {"", "Spina"},
            {"", "Belser"}, {"", "Eichenbronner"}, {"", "Rosenfeld"}, {"", "Olivera"},
            {"", "Berardi"}, {"", "Querales"}, {"", "Kandel"}, {"", "Tordoni"},
            {"", "Derman"}, {"", "Banfi"}, {"", "Almagro"}, {"", "Luccon"},
            {"", "Benítez"}, {"", "Fernández Armendáriz"}, {"", "Ferraro"}, {"", "Roldán"},
            {"", "Chervonko"}, {"", "Asereny"}, {"", "Longobardi"}, {"", "Accardo"},
            {"", "Durán"}, {"", "Ferrari"}, {"", "Veneroso"}, {"", "Gentile"},
            {"", "Ambao"}, {"", "Villarejo"}, {"", "Rivas"}, {"", "Veronelli"},
            {"", "Verdelli"}, {"", "Prado"}, {"", "Rivarola"}, {"", "Muiño"},
            {"", "Dayan"}, {"", "Esmoris"}, {"", "Kirsanov"}, {"", "Plencovich"},
            {"", "Ferreyra Fernández"}, {"", "Jacovkis"}, {"", "Curatolo"}, {"", "Carrió"},
            {"", "Ertel"}, {"", "Adem"}, {"", "Clessi"}, {"", "Tabakian"},
            {"", "Arriagada"}, {"", "Rodrigues Da Silva"}, {"", "Perduca"},
            {"", "De Carlos"}, {"", "Raviolo"}, {"", "Cabral"}, {"", "Zito Lema"},
            {"", "Otero"},
    };

    /**
     * Cátedras del año 2026 (unión del 1er y 2do cuatrimestre): la materia
     * seguida de los profesores que la dictan (clave "inicial apellido" cuando
     * hay inicial). Varias materias "pares" rotan docentes entre cuatrimestres
     * (Taller 1↔2, 3↔4, 5↔6, Didáctica Específica 1↔2, Creatividad 1↔2,
     * Literatura 1↔2, Cultura 2↔3, Ab Initio 1↔2): por eso el mismo profesor
     * aparece en ambas materias del par. Las materias que no aparecen no se
     * dictaron en 2026 (las residencias) y quedan sin cátedras.
     */
    private static final String[][] CATEDRAS = {
            // ----- 1er año -----
            {"Lengua Inglesa 1", "De Domenico", "Ragno", "Crottogini", "Costa"},
            {"Gramática Inglesa 1", "Mortoro", "Rossell", "V. Fernandez", "Rodríguez", "Caligaris"},
            {"Fonología y Práctica de Laboratorio 1", "Pérez Ponsa", "Massolo", "Carteau", "Morelli"},
            {"Taller 1", "Bessega", "Castino", "Jaschek", "F. Costa"},
            {"Taller 2", "Jaschek", "Bessega", "F. Costa", "Castino"},
            {"Pedagogía", "Del Regno", "Bergel", "Belser"},
            {"Didáctica General", "Spina", "Belser", "Del Regno"},
            {"Psicología Educacional", "Eichenbronner", "Rosenfeld"},
            {"Taller de Nuevas Tecnologías", "Olivera", "Berardi"},
            {"Taller de Lectura, Escritura y Oralidad (TLEO)", "Querales", "Kandel", "Cabral"},
            // ----- 2do año -----
            {"Lengua Inglesa 2", "Tordoni", "Crottogini", "Derman"},
            {"Gramática Inglesa 2", "Banfi", "Almagro"},
            {"Fonología y Práctica de Laboratorio 2", "Carteau", "Luccon", "Benítez"},
            {"Cultura de los Pueblos de Habla Inglesa 1", "Fernández Armendáriz", "Ferraro", "Roldán"},
            {"Didáctica Específica 1", "Chervonko", "Asereny", "Longobardi"},
            {"Didáctica Específica 2", "Longobardi", "Chervonko", "Asereny"},
            {"Creatividad 1", "Accardo", "Durán"},
            {"Taller 3", "Bessega", "Castino", "F. Costa"},
            {"Taller 4", "Castino", "F. Costa", "Bessega"},
            {"Sujetos de la Educación 1", "Ferrari", "Veneroso"},
            {"Sistema y Política Educativa", "Gentile", "Del Regno", "Ambao"},
            {"Metodología de la Investigación", "Villarejo", "Rivas", "Zito Lema"},
            {"Educación Sexual Integral (ESI)", "Veronelli", "Accardo"},
            // ----- 3er año -----
            {"Lengua Inglesa 3", "Caligaris", "Longobardi"},
            {"Lingüística", "Caligaris", "Verdelli"},
            {"Fonología y Práctica de Laboratorio 3", "Massolo", "Luccon", "Prado"},
            {"Literatura en Lengua Inglesa 1", "Costa"},
            {"Literatura en Lengua Inglesa 2", "Costa"},
            {"Sujetos de la Educación 2", "Rivarola", "Otero"},
            {"Creatividad 2", "Durán", "Accardo"},
            {"Taller 5 (de Inicial y Primaria)", "Muiño", "Castino"},
            {"Seminario de Investigación Acción 1", "Banfi"},
            {"Informática para la Enseñanza", "Berardi", "Dayan"},
            {"Instituciones Educativas", "Esmoris", "Kirsanov", "Gentile"},
            {"Saberes Lúdicos, Corporales y Motores", "Plencovich"},
            // Trabajo de Campo se cursa "en" una materia del CFG: una cátedra
            // por cada profesor que lo recibe.
            {"Trabajo de Campo", "Del Regno", "Eichenbronner", "Esmoris", "Gentile",
                    "Belser", "Spina", "Kirsanov", "Ambao", "Rosenfeld", "Bergel"},
            // ----- 4to año -----
            {"Lengua Inglesa 4", "Ferreyra Fernández", "Longobardi"},
            {"Cultura de los Pueblos de Habla Inglesa 2", "Fernández Armendáriz", "Perduca"},
            {"Literatura en Lengua Inglesa 3", "Fernández Armendáriz", "Jacovkis"},
            {"Análisis y Redacción de Textos", "Curatolo", "Carrió"},
            {"Teatro", "Ertel"},
            {"Taller 6 (de Nivel Medio)", "Castino", "Muiño"},
            {"Seminario de Investigación Acción 2", "Adem"},
            {"Nuevos Escenarios, Cultura, Tecnología y Subjetividad", "Villarejo", "Tabakian"},
            {"Trabajo / Profesionalización Docente", "Clessi", "Rivas"},
            {"Filosofía", "Tabakian", "Arriagada"},
            {"TIC Aplicadas", "Dayan"},
            // ----- 5to año -----
            {"Portugués Ab Initio 1", "Rodrigues Da Silva"},
            {"Portugués Ab Initio 2", "Rodrigues Da Silva"},
            {"Cultura de los Pueblos de Habla Inglesa 3", "Fernández Armendáriz", "Perduca"},
            {"Literatura en Lengua Inglesa 4", "Fernández Armendáriz", "Jacovkis"},
            {"Taller de Música", "De Carlos", "Raviolo"},
    };

    @Override
    public void run(String... args) {
        seedCatalogo();
        seedAdmin();
    }

    /**
     * Garantiza que exista al menos un admin. Corre SIEMPRE (no solo con la base
     * vacía): las bases creadas antes de que existiera el rol también lo
     * necesitan. Si el DNI ya estaba registrado como usuario común, lo promueve.
     */
    private void seedAdmin() {
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        Usuario admin = usuarioRepository.findByDni("99999999").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre("Admin");
            u.setDni("99999999");
            return u;
        });
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
    }

    private void seedCatalogo() {
        // Si ya hay datos, no sembramos de nuevo.
        if (materiaRepository.count() > 0) {
            return;
        }

        // ===== Materias =====
        Map<String, Materia> materias = new LinkedHashMap<>();
        for (Object[] dato : MATERIAS) {
            Materia m = new Materia();
            m.setAnio((Integer) dato[0]);
            m.setNombre((String) dato[1]);
            materias.put(m.getNombre(), m);
        }
        materiaRepository.saveAll(materias.values());

        // ===== Profesores =====
        // Clave "inicial apellido" (o solo apellido) para referenciarlos abajo.
        Map<String, Profesor> profesores = new LinkedHashMap<>();
        for (String[] dato : PROFESORES) {
            Profesor p = new Profesor();
            p.setNombre(dato[0]);
            p.setApellido(dato[1]);
            profesores.put((dato[0] + " " + dato[1]).trim(), p);
        }
        profesorRepository.saveAll(profesores.values());

        // ===== Cátedras =====
        // requireNonNull: un typo en un nombre debe frenar el arranque con un
        // mensaje claro, no sembrar un catálogo a medias.
        List<Catedra> catedras = new ArrayList<>();
        for (String[] fila : CATEDRAS) {
            Materia materia = Objects.requireNonNull(
                    materias.get(fila[0]), "Materia no declarada: " + fila[0]);
            for (int i = 1; i < fila.length; i++) {
                Catedra c = new Catedra();
                c.setMateria(materia);
                c.setProfesor(Objects.requireNonNull(
                        profesores.get(fila[i]), "Profesor no declarado: " + fila[i]));
                catedras.add(c);
            }
        }
        catedraRepository.saveAll(catedras);

        // ===== Usuario de prueba (login con DNI "12345678") =====
        Usuario usuario = new Usuario();
        usuario.setNombre("Estudiante Test");
        usuario.setDni("12345678");
        usuarioRepository.save(usuario);
    }
}
