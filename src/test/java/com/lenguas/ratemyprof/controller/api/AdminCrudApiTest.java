package com.lenguas.ratemyprof.controller.api;

import com.lenguas.ratemyprof.config.SecurityConfig;
import com.lenguas.ratemyprof.dto.MateriaView;
import com.lenguas.ratemyprof.exception.ApiExceptionHandler;
import com.lenguas.ratemyprof.exception.ConflictException;
import com.lenguas.ratemyprof.model.Profesor;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.MateriaRepository;
import com.lenguas.ratemyprof.repository.ProfesorRepository;
import com.lenguas.ratemyprof.service.AdminService;
import com.lenguas.ratemyprof.service.CatedraService;
import com.lenguas.ratemyprof.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest del CRUD de catálogo (2.6). Lo central acá es la AUTORIZACIÓN:
 * la regla hasRole("ADMIN") de SecurityConfig sobre las escrituras de
 * /api/materias|profesores|catedras. Por eso cada operación se prueba en tres
 * sabores: sin sesión (401), con usuario común (403) y como admin (2xx).
 * La lógica de negocio (409 por duplicados/borrados con datos) está mockeada:
 * vive en AdminService y acá solo se verifica su traducción a HTTP.
 */
@WebMvcTest({MateriaApiController.class, ProfesorApiController.class, CatedraApiController.class})
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AdminCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;
    @MockBean
    private MateriaRepository materiaRepository;
    @MockBean
    private ProfesorRepository profesorRepository;
    @MockBean
    private CatedraRepository catedraRepository;
    @MockBean
    private CatedraService catedraService;
    @MockBean
    private ReviewService reviewService;

    // ---------- autorización por rol ----------

    @Test
    void crearMateria_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(post("/api/materias")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\": \"Fonética II\", \"anio\": 2}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser // usuario común: rol USER, no alcanza
    void crearMateria_usuarioComun_devuelve403() throws Exception {
        mockMvc.perform(post("/api/materias")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\": \"Fonética II\", \"anio\": 2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearMateria_admin_devuelve201() throws Exception {
        when(adminService.crearMateria("Fonética II", 2))
                .thenReturn(new MateriaView(10L, "Fonética II", 2));

        mockMvc.perform(post("/api/materias")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\": \"Fonética II\", \"anio\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nombre").value("Fonética II"))
                .andExpect(jsonPath("$.anio").value(2));
    }

    // Los GET siguen públicos aun después de sumar la regla de ADMIN.
    @Test
    void listarProfesores_sinSesion_devuelve200() throws Exception {
        when(profesorRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(profesor(1L, "Ana", "Martínez")));

        mockMvc.perform(get("/api/profesores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].apellido").value("Martínez"));
    }

    // ---------- validación y conflictos ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearMateria_nombreVacio_devuelve400() throws Exception {
        // Con anio válido: el único error de campos tiene que ser el nombre.
        mockMvc.perform(post("/api/materias")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\": \"\", \"anio\": 2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void borrarMateria_conCatedras_devuelve409() throws Exception {
        doThrow(new ConflictException("La materia tiene cátedras asociadas: borrálas primero"))
                .when(adminService).borrarMateria(1L);

        mockMvc.perform(delete("/api/materias/1").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCatedra_duplicada_devuelve409() throws Exception {
        when(adminService.crearCatedra(1L, 2L))
                .thenThrow(new ConflictException("Ese profesor ya tiene una cátedra en esa materia"));

        mockMvc.perform(post("/api/catedras")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"profesorId\": 1, \"materiaId\": 2}"))
                .andExpect(status().isConflict());
    }

    // ---------- caso feliz de edición/borrado ----------

    @Test
    @WithMockUser(roles = "ADMIN")
    void editarProfesor_admin_devuelve204() throws Exception {
        mockMvc.perform(put("/api/profesores/5")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\": \"Ana\", \"apellido\": \"Suárez\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void borrarCatedra_admin_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/catedras/3").with(csrf()))
                .andExpect(status().isNoContent());
    }

    private Profesor profesor(Long id, String nombre, String apellido) {
        Profesor p = new Profesor();
        p.setId(id);
        p.setNombre(nombre);
        p.setApellido(apellido);
        return p;
    }
}
