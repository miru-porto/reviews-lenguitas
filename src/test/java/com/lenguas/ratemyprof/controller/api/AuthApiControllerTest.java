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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest de la autenticación de la API. Verifica: login con credenciales
 * malas → 401, registro con datos inválidos → 400, y /me según haya o no sesión.
 */
@WebMvcTest(AuthApiController.class)
@Import({SecurityConfig.class, ApiExceptionHandler.class})
class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UsuarioService usuarioService;

    private static final String EMAIL = "ana@ejemplo.com";

    // ---------- login ----------

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest req = new LoginRequest();
        req.setEmail(EMAIL);
        req.setPassword("incorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Email o contraseña incorrectos"));
    }

    // ---------- registro ----------

    @Test
    void registro_emailInvalido_devuelve400() throws Exception {
        RegistroRequest req = new RegistroRequest();
        req.setNombre("Ana");
        req.setEmail("no-es-un-email");
        req.setPassword("secreta123");

        mockMvc.perform(post("/api/auth/registro")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos inválidos"))
                .andExpect(jsonPath("$.campos.email").exists());
    }

    // ---------- me ----------

    @Test
    void me_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void me_conSesion_devuelveUsuario() throws Exception {
        Usuario ana = new Usuario();
        ana.setId(1L);
        ana.setNombre("Ana");
        ana.setEmail(EMAIL);
        when(usuarioService.findByEmail(EMAIL)).thenReturn(ana);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }
}
