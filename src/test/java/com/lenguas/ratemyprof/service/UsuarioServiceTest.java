package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.model.Rol;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import com.lenguas.ratemyprof.repository.UsuarioRepository;
import com.lenguas.ratemyprof.repository.VotoUtilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Tests de UsuarioService: el alta por Google (que decide el rol) y la baja de
 * cuenta (donde el ORDEN de borrado es lo que importa).
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private VotoUtilRepository votoUtilRepository;

    @InjectMocks private UsuarioService usuarioService;

    private static final String SUB = "104512345678901234567";
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @BeforeEach
    void setUp() {
        // @Value no se resuelve fuera del contexto de Spring.
        ReflectionTestUtils.setField(usuarioService, "adminEmail", ADMIN_EMAIL);
    }

    // ---------- ingreso ----------

    @Test
    void ingresarConGoogle_emailDeAdmin_daRolAdmin() {
        when(usuarioRepository.findByGoogleSub(SUB)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = usuarioService.ingresarConGoogle(SUB, ADMIN_EMAIL);

        assertThat(u.getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void ingresarConGoogle_otroEmail_daRolUser() {
        when(usuarioRepository.findByGoogleSub(SUB)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = usuarioService.ingresarConGoogle(SUB, "cualquiera@gmail.com");

        assertThat(u.getRol()).isEqualTo(Rol.USER);
    }

    /** El apodo se pide aparte: Google no lo decide. */
    @Test
    void ingresarConGoogle_usuarioNuevo_quedaSinApodo() {
        when(usuarioRepository.findByGoogleSub(SUB)).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = usuarioService.ingresarConGoogle(SUB, "cualquiera@gmail.com");

        assertThat(u.getNombre()).isNull();
    }

    /** Si dejara de ser el admin, tiene que perder el rol al reingresar. */
    @Test
    void ingresarConGoogle_exAdmin_pierdeElRol() {
        Usuario existente = usuario(Rol.ADMIN);
        when(usuarioRepository.findByGoogleSub(SUB)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = usuarioService.ingresarConGoogle(SUB, "yaNoSoyAdmin@gmail.com");

        assertThat(u.getRol()).isEqualTo(Rol.USER);
    }

    // ---------- baja ----------

    /**
     * El orden es una restricción de las FK, no una preferencia: los votos ajenos
     * sobre mis reviews tienen que irse ANTES que las reviews, y las reviews antes
     * que yo. Si alguien reordena esto, la baja explota en producción con datos
     * reales — y con la base vacía de un test de integración no se notaría.
     */
    @Test
    void borrarCuenta_borraEnElOrdenQuePermitenLasForeignKeys() {
        Usuario yo = usuario(Rol.USER);
        when(usuarioRepository.findByGoogleSub(SUB)).thenReturn(Optional.of(yo));

        usuarioService.borrarCuenta(SUB);

        InOrder orden = inOrder(votoUtilRepository, reviewRepository, usuarioRepository);
        orden.verify(votoUtilRepository).borrarVotosSobreReviewsDe(1L);
        orden.verify(votoUtilRepository).borrarVotosDe(1L);
        orden.verify(reviewRepository).borrarReviewsDe(1L);
        orden.verify(usuarioRepository).delete(yo);
    }

    // ---------- helpers ----------

    private static Usuario usuario(Rol rol) {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setGoogleSub(SUB);
        u.setEmail("ana@gmail.com");
        u.setRol(rol);
        return u;
    }
}
