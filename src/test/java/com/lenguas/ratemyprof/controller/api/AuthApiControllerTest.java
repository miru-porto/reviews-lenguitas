package com.lenguas.ratemyprof.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lenguas.ratemyprof.config.SecurityConfig;
import com.lenguas.ratemyprof.dto.LoginRequest;
import com.lenguas.ratemyprof.dto.RegistroRequest;
import com.lenguas.ratemyprof.exception.ApiExceptionHandler;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest de la autenticación de la API (login por DNI, sin contraseña).
 * Verifica: login con un DNI no registrado → 404, registro con un DNI de formato
 * inválido → 400, y /me según haya o no sesión. El principal es el DNI, así que
 * el @WithMockUser usa un DNI como username.
 */
@WebMvcTest(AuthApiController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // La API chain define un AuthenticationManager (bean de SecurityConfig); lo
    // mockeamos para no depender del cableado real en este slice, aunque el login
    // por DNI ya no lo use.
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UsuarioService usuarioService;

    private static final String DNI = "30111222";

    // ---------- login ----------

    @Test
    void login_dniNoRegistrado_devuelve404() throws Exception {
        when(usuarioService.buscarPorDni(DNI)).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setDni(DNI);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DNI no registrado"));
    }

    // ---------- registro ----------

    @Test
    void registro_dniInvalido_devuelve400() throws Exception {
        RegistroRequest req = new RegistroRequest();
        req.setDni("no-es-un-dni");   // no matchea \d{7,8}
        req.setNombre("Ana");

        mockMvc.perform(post("/api/auth/registro")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.campos.dni").exists());
    }

    // ---------- me ----------

    @Test
    void me_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = DNI)
    void me_conSesion_devuelveUsuario() throws Exception {
        Usuario ana = new Usuario();
        ana.setId(1L);
        ana.setDni(DNI);
        ana.setNombre("Ana");
        when(usuarioService.findByDni(DNI)).thenReturn(ana);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dni").value(DNI))
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }
}
