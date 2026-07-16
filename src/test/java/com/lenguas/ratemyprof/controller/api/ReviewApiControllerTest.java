package com.lenguas.ratemyprof.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lenguas.ratemyprof.config.SecurityConfig;
import com.lenguas.ratemyprof.dto.CrearReviewRequest;
import com.lenguas.ratemyprof.dto.ReviewForm;
import com.lenguas.ratemyprof.exception.ApiExceptionHandler;
import com.lenguas.ratemyprof.exception.ForbiddenException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.ReviewService;
import com.lenguas.ratemyprof.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest de la API de escritura de reviews: levanta solo la capa web + la
 * SecurityConfig real (dos filter chains), con los services mockeados. Verifica
 * los códigos de estado que la fase 2 promete: 401 sin sesión, 400 con body
 * inválido, 403 sobre review ajena, y los caminos felices.
 *
 * Detalle CSRF: la API tiene CSRF activo, así que las mutaciones llevan
 * .with(csrf()); sin él Spring cortaría con 403 antes de mirar la autorización.
 */
@WebMvcTest(ReviewApiController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class ReviewApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;
    @MockBean
    private UsuarioService usuarioService;
    // La API chain define un AuthenticationManager; lo mockeamos para no depender
    // del cableado real de autenticación en este slice.
    @MockBean
    private AuthenticationManager authenticationManager;

    private static final String DNI = "30111222";

    private CrearReviewRequest crearRequest() {
        CrearReviewRequest req = new CrearReviewRequest();
        req.setCatedraId(10L);
        req.setPuntuacion(5);
        req.setComentario("Excelente cursada");
        req.setCuatrimestre("1C 2026");
        return req;
    }

    // ---------- 401 sin sesión ----------

    @Test
    void post_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isUnauthorized());
    }

    // ---------- 400 body inválido ----------

    @Test
    @WithMockUser(username = DNI)
    void post_bodyInvalido_devuelve400ConCampos() throws Exception {
        CrearReviewRequest req = crearRequest();
        req.setComentario("");   // @NotBlank
        req.setPuntuacion(9);    // @Max(5)

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.campos.comentario").exists())
                .andExpect(jsonPath("$.campos.puntuacion").exists());
    }

    // ---------- 201 feliz ----------

    @Test
    @WithMockUser(username = DNI)
    void post_feliz_devuelve201ConId() throws Exception {
        Usuario ana = new Usuario();
        ana.setId(1L);
        ana.setDni(DNI);
        Catedra catedra = new Catedra();
        catedra.setId(10L);
        Review creada = new Review();
        creada.setId(42L);
        creada.setCatedra(catedra);
        creada.setUsuario(ana);

        when(usuarioService.findByDni(DNI)).thenReturn(ana);
        when(reviewService.crear(eq(10L), eq(ana), eq(5), any(), any())).thenReturn(creada);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.catedraId").value(10));
    }

    // ---------- 403 review ajena ----------

    @Test
    @WithMockUser(username = DNI)
    void put_reviewAjena_devuelve403() throws Exception {
        Usuario ana = new Usuario();
        ana.setId(1L);
        ana.setDni(DNI);
        when(usuarioService.findByDni(DNI)).thenReturn(ana);
        when(reviewService.editar(anyLong(), any(), any(), any(), any()))
                .thenThrow(new ForbiddenException("No tenés permiso para modificar esta review"));

        ReviewForm form = new ReviewForm();
        form.setPuntuacion(3);
        form.setComentario("Editado");
        form.setCuatrimestre("1C 2026");

        mockMvc.perform(put("/api/reviews/5")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("No tenés permiso para modificar esta review"));
    }

    // ---------- 204 borrar ----------

    @Test
    @WithMockUser(username = DNI)
    void delete_feliz_devuelve204() throws Exception {
        Usuario ana = new Usuario();
        ana.setId(1L);
        ana.setDni(DNI);
        when(usuarioService.findByDni(DNI)).thenReturn(ana);
        when(reviewService.eliminar(eq(5L), eq(ana))).thenReturn(10L);

        mockMvc.perform(delete("/api/reviews/5").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
