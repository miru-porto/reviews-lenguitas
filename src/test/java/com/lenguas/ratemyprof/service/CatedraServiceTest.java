package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.CatedraView;
import com.lenguas.ratemyprof.dto.NivelRating;
import com.lenguas.ratemyprof.dto.RatingBreakdown;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.CatedraConRating;
import com.lenguas.ratemyprof.model.Materia;
import com.lenguas.ratemyprof.model.Profesor;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatedraServiceTest {

    @Mock
    private CatedraRepository catedraRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CatedraService catedraService;

    @Test
    void findById_lanzaNotFoundSiNoExiste() {
        when(catedraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catedraService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findViewById_aplanaProfesorYMateria() {
        Materia materia = new Materia();
        materia.setId(3L);
        materia.setNombre("Fonética");
        Profesor profesor = new Profesor();
        profesor.setNombre("Ana");
        profesor.setApellido("García");
        Catedra catedra = new Catedra();
        catedra.setId(10L);
        catedra.setMateria(materia);
        catedra.setProfesor(profesor);
        when(catedraRepository.findById(10L)).thenReturn(Optional.of(catedra));

        CatedraView view = catedraService.findViewById(10L);

        assertThat(view.getCatedraId()).isEqualTo(10L);
        assertThat(view.getMateriaId()).isEqualTo(3L);
        assertThat(view.getMateriaNombre()).isEqualTo("Fonética");
        assertThat(view.getNombreProfesor()).isEqualTo("Ana");
        assertThat(view.getApellidoProfesor()).isEqualTo("García");
    }

    @Test
    void findByMateriaOrdenadoPorRating_delegaEnLaQueryDelRepositorio() {
        List<CatedraConRating> esperado = List.of(
                new CatedraConRating(1L, "Ana", "García", "Fonética", 4.5, 2L));
        when(catedraRepository.findByMateriaConRating(3L)).thenReturn(esperado);

        assertThat(catedraService.findByMateriaOrdenadoPorRating(3L)).isSameAs(esperado);
    }

    @Test
    void desgloseRating_calculaPromedioTotalYPorcentajes() {
        // 2 reviews de 5★, 1 de 4★, 1 de 1★ (las de 3★ y 2★ no vienen en el GROUP BY).
        when(reviewRepository.contarPorPuntuacion(10L)).thenReturn(List.<Object[]>of(
                new Object[]{5, 2L},
                new Object[]{4, 1L},
                new Object[]{1, 1L}));
        when(reviewRepository.promedioByCatedraId(10L)).thenReturn(3.75);

        RatingBreakdown desglose = catedraService.desgloseRating(10L);

        assertThat(desglose.getPromedio()).isEqualTo(3.75);
        assertThat(desglose.getTotal()).isEqualTo(4);
        // Siempre 5 niveles, de 5★ a 1★, con 0 en los que no tienen reviews.
        assertThat(desglose.getNiveles())
                .extracting(NivelRating::getEstrellas)
                .containsExactly(5, 4, 3, 2, 1);
        assertThat(desglose.getNiveles())
                .extracting(NivelRating::getCantidad)
                .containsExactly(2L, 1L, 0L, 0L, 1L);
        assertThat(desglose.getNiveles())
                .extracting(NivelRating::getPorcentaje)
                .containsExactly(50, 25, 0, 0, 25);
    }

    @Test
    void desgloseRating_sinReviewsDevuelveTodoEnCero() {
        when(reviewRepository.contarPorPuntuacion(10L)).thenReturn(List.of());
        when(reviewRepository.promedioByCatedraId(10L)).thenReturn(null);

        RatingBreakdown desglose = catedraService.desgloseRating(10L);

        assertThat(desglose.getPromedio()).isEqualTo(0.0);
        assertThat(desglose.getTotal()).isZero();
        assertThat(desglose.getNiveles()).hasSize(5)
                .allSatisfy(nivel -> {
                    assertThat(nivel.getCantidad()).isZero();
                    assertThat(nivel.getPorcentaje()).isZero();
                });
    }
}
