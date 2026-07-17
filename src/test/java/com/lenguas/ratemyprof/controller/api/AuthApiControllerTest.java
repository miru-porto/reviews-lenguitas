package com.lenguas.ratemyprof.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lenguas.ratemyprof.config.GoogleOidcUserService;
import com.lenguas.ratemyprof.config.SecurityConfig;
import com.lenguas.ratemyprof.dto.ApodoRequest;
import com.lenguas.ratemyprof.exception.ApiExceptionHandler;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest de lo que queda de la auth con el login por Google: /me y el alta
 * de apodo. El ingreso en sí no se prueba acá — lo maneja entero Spring Security
 * contra Google y no hay controller propio que ejercitar.
 *
 * El principal es el 'sub' de Google, así que el @WithMockUser lo usa como
 * username: es lo que devuelve auth.getName() en producción.
 */
@WebMvcTest(AuthApiController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;
    // SecurityConfig lo pide para armar el oauth2Login; el slice web no lo trae
    // solo porque es un @Service.
    @MockBean
    private GoogleOidcUserService googleOidcUserService;

    private static final String SUB = "104512345678901234567";

    // ---------- me ----------

    @Test
    void me_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = SUB)
    void me_conSesion_devuelveUsuario() throws Exception {
        when(usuarioService.findByGoogleSub(SUB)).thenReturn(usuario("Ana"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    /** El email y el id de Google no son asunto del front: no deben viajar. */
    @Test
    @WithMockUser(username = SUB)
    void me_noExponeEmailNiGoogleSub() throws Exception {
        when(usuarioService.findByGoogleSub(SUB)).thenReturn(usuario("Ana"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.googleSub").doesNotExist());
    }

    /** Recién entrada por Google y sin apodo: el front lo usa para pedirlo. */
    @Test
    @WithMockUser(username = SUB)
    void me_sinApodo_devuelveNombreNull() throws Exception {
        when(usuarioService.findByGoogleSub(SUB)).thenReturn(usuario(null));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").doesNotExist());
    }

    // ---------- apodo ----------

    @Test
    @WithMockUser(username = SUB)
    void apodo_valido_devuelveUsuarioActualizado() throws Exception {
        when(usuarioService.elegirApodo(eq(SUB), eq("Ana"))).thenReturn(usuario("Ana"));

        mockMvc.perform(put("/api/auth/apodo")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(apodoRequest("Ana"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    @WithMockUser(username = SUB)
    void apodo_vacio_devuelve400() throws Exception {
        mockMvc.perform(put("/api/auth/apodo")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(apodoRequest("  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.campos.apodo").exists());
    }

    @Test
    void apodo_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(put("/api/auth/apodo")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(apodoRequest("Ana"))))
                .andExpect(status().isUnauthorized());
    }

    // ---------- helpers ----------

    private static Usuario usuario(String apodo) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setGoogleSub(SUB);
        u.setEmail("ana@gmail.com");
        u.setNombre(apodo);
        return u;
    }

    private static ApodoRequest apodoRequest(String apodo) {
        ApodoRequest req = new ApodoRequest();
        req.setApodo(apodo);
        return req;
    }
}
