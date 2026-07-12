package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.model.VotoUtil;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import com.lenguas.ratemyprof.repository.VotoUtilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests de ReviewService: mockeamos los repositories (Mockito) para
 * testear la lógica sin base de datos. El caso central es la autorización:
 * nadie puede editar/borrar una review ajena, aunque manipule la URL.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private CatedraRepository catedraRepository;
    @Mock
    private VotoUtilRepository votoUtilRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Usuario ana;    // dueña de la review
    private Usuario belen;  // otra usuaria
    private Catedra catedra;
    private Review review;

    @BeforeEach
    void setUp() {
        ana = usuario(1L, "Ana", "11111111");
        belen = usuario(2L, "Belén", "22222222");

        catedra = new Catedra();
        catedra.setId(10L);

        review = new Review();
        review.setId(5L);
        review.setUsuario(ana);
        review.setCatedra(catedra);
        review.setPuntuacion(4);
        review.setComentario("Muy buena cursada");
    }

    // ---------- obtenerPropia: la barrera de autorización ----------

    @Test
    void obtenerPropia_devuelveLaReviewSiEsDelUsuario() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        assertThat(reviewService.obtenerPropia(5L, ana)).isSameAs(review);
    }

    @Test
    void obtenerPropia_rechazaUnaReviewAjena() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.obtenerPropia(5L, belen))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permiso");
    }

    @Test
    void obtenerPropia_lanzaNotFoundSiLaReviewNoExiste() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.obtenerPropia(99L, ana))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------- editar / eliminar ----------

    @Test
    void editar_actualizaYDevuelveElIdDeLaCatedra() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        Long catedraId = reviewService.editar(5L, ana, 5, "Editado");

        assertThat(catedraId).isEqualTo(10L);
        assertThat(review.getPuntuacion()).isEqualTo(5);
        assertThat(review.getComentario()).isEqualTo("Editado");
        verify(reviewRepository).save(review);
    }

    @Test
    void eliminar_borraLosVotosAntesQueLaReview() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        Long catedraId = reviewService.eliminar(5L, ana);

        assertThat(catedraId).isEqualTo(10L);
        // El orden importa: la FK de votos_util impide borrar la review primero.
        InOrder enOrden = inOrder(votoUtilRepository, reviewRepository);
        enOrden.verify(votoUtilRepository).deleteByReviewId(5L);
        enOrden.verify(reviewRepository).delete(review);
    }

    // ---------- crear ----------

    @Test
    void crear_rechazaSegundaReviewDelMismoUsuarioEnLaCatedra() {
        when(reviewRepository.existsByUsuarioIdAndCatedraId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.crear(10L, ana, 4, "Otra más"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya dejaste");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void crear_guardaLaReviewConSusDatos() {
        when(reviewRepository.existsByUsuarioIdAndCatedraId(1L, 10L)).thenReturn(false);
        when(catedraRepository.findById(10L)).thenReturn(Optional.of(catedra));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review creada = reviewService.crear(10L, ana, 5, "Excelente");

        assertThat(creada.getUsuario()).isSameAs(ana);
        assertThat(creada.getCatedra()).isSameAs(catedra);
        assertThat(creada.getPuntuacion()).isEqualTo(5);
        assertThat(creada.getComentario()).isEqualTo("Excelente");
        assertThat(creada.getFechaCreacion()).isNotNull();
    }

    @Test
    void crear_lanzaNotFoundSiLaCatedraNoExiste() {
        when(reviewRepository.existsByUsuarioIdAndCatedraId(1L, 99L)).thenReturn(false);
        when(catedraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.crear(99L, ana, 4, "Hola"))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------- votarUtil (toggle) ----------

    @Test
    void votarUtil_rechazaVotarLaReviewPropia() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.votarUtil(5L, ana))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("propia");
        verify(votoUtilRepository, never()).save(any());
    }

    @Test
    void votarUtil_creaElVotoSiNoHabiaVotado() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));
        when(votoUtilRepository.findByUsuarioIdAndReviewId(2L, 5L)).thenReturn(Optional.empty());

        Long catedraId = reviewService.votarUtil(5L, belen);

        assertThat(catedraId).isEqualTo(10L);
        ArgumentCaptor<VotoUtil> captor = ArgumentCaptor.forClass(VotoUtil.class);
        verify(votoUtilRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(belen);
        assertThat(captor.getValue().getReview()).isSameAs(review);
    }

    @Test
    void votarUtil_quitaElVotoSiYaExistia() {
        VotoUtil votoExistente = new VotoUtil();
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review));
        when(votoUtilRepository.findByUsuarioIdAndReviewId(2L, 5L))
                .thenReturn(Optional.of(votoExistente));

        reviewService.votarUtil(5L, belen);

        verify(votoUtilRepository).delete(votoExistente);
        verify(votoUtilRepository, never()).save(any());
    }

    // ---------- findByCatedra: mapeo a view model + orden + paginación ----------

    @Test
    void findByCatedra_mapeaViewsYConservaLosDatosDePaginacion() {
        Review r2 = new Review();
        r2.setId(6L);
        r2.setUsuario(belen);
        r2.setCatedra(catedra);
        r2.setPuntuacion(3);
        r2.setComentario("Regular");
        r2.setFechaCreacion(LocalDateTime.of(2026, 6, 30, 12, 0));
        review.setFechaCreacion(LocalDateTime.of(2026, 7, 1, 12, 0));

        // El orden por votos lo resuelve la base: con orden=utiles el service
        // tiene que usar la query de votos útiles. Simulamos que esta página de
        // tamaño 2 es la primera de 7 reviews en total.
        PageRequest pagina = PageRequest.of(0, 2);
        when(reviewRepository.findByCatedraIdOrdenVotosUtiles(10L, pagina))
                .thenReturn(new PageImpl<>(List.of(r2, review), pagina, 7));
        // r2 tiene 3 votos útiles; la review de Ana, ninguno.
        when(votoUtilRepository.contarPorReviewDeCatedra(10L))
                .thenReturn(List.<Object[]>of(new Object[]{6L, 3L}));
        // Ana ya votó r2 como útil.
        when(votoUtilRepository.reviewIdsVotadasPor("11111111", 10L))
                .thenReturn(List.of(6L));

        Page<ReviewView> vistas = reviewService.findByCatedra(10L, "11111111", "utiles", pagina);

        assertThat(vistas).extracting(ReviewView::getId).containsExactly(6L, 5L);
        // El mapeo a views no pierde los metadatos de la página.
        assertThat(vistas.getTotalElements()).isEqualTo(7);
        assertThat(vistas.getTotalPages()).isEqualTo(4);
        assertThat(vistas.getNumber()).isZero();

        ReviewView deBelen = vistas.getContent().get(0);
        assertThat(deBelen.getAutor()).isEqualTo("Belén");
        assertThat(deBelen.getVotosUtil()).isEqualTo(3);
        assertThat(deBelen.isEsMia()).isFalse();
        assertThat(deBelen.isLaVoteUtil()).isTrue();
        assertThat(deBelen.getFecha()).isEqualTo("30/06/2026");

        ReviewView deAna = vistas.getContent().get(1);
        assertThat(deAna.isEsMia()).isTrue();
        assertThat(deAna.getVotosUtil()).isZero();
    }

    @Test
    void findByCatedra_conOrdenFechaUsaLaQueryPorFecha() {
        PageRequest pagina = PageRequest.of(0, 5);
        when(reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(10L, pagina))
                .thenReturn(Page.empty(pagina));
        when(votoUtilRepository.contarPorReviewDeCatedra(10L)).thenReturn(List.of());

        Page<ReviewView> vistas = reviewService.findByCatedra(10L, null, "fecha", pagina);

        assertThat(vistas).isEmpty();
        verify(reviewRepository, never()).findByCatedraIdOrdenVotosUtiles(any(), any());
    }

    // ---------- yaReviewo ----------

    @Test
    void yaReviewo_devuelveFalseSinSesionSinTocarLaBase() {
        assertThat(reviewService.yaReviewo(10L, null)).isFalse();
        verify(reviewRepository, never()).existsByUsuarioDniAndCatedraId(any(), any());
    }

    @Test
    void yaReviewo_consultaPorDniYCatedra() {
        when(reviewRepository.existsByUsuarioDniAndCatedraId("11111111", 10L)).thenReturn(true);

        assertThat(reviewService.yaReviewo(10L, "11111111")).isTrue();
    }

    // ---------- helpers ----------

    private static Usuario usuario(Long id, String nombre, String dni) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombre(nombre);
        u.setDni(dni);
        return u;
    }
}
