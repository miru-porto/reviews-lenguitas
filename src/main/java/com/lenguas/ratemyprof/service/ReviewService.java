package com.lenguas.ratemyprof.service;

import com.lenguas.ratemyprof.dto.ReviewView;
import com.lenguas.ratemyprof.exception.BadRequestException;
import com.lenguas.ratemyprof.exception.ConflictException;
import com.lenguas.ratemyprof.exception.ForbiddenException;
import com.lenguas.ratemyprof.exception.NotFoundException;
import com.lenguas.ratemyprof.model.Catedra;
import com.lenguas.ratemyprof.model.Cuatrimestre;
import com.lenguas.ratemyprof.model.Review;
import com.lenguas.ratemyprof.model.Usuario;
import com.lenguas.ratemyprof.model.VotoUtil;
import com.lenguas.ratemyprof.repository.CatedraRepository;
import com.lenguas.ratemyprof.repository.ReviewRepository;
import com.lenguas.ratemyprof.repository.VotoUtilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Valor del parámetro "orden" para ordenar por votos útiles. */
    public static final String ORDEN_UTILES = "utiles";

    private final ReviewRepository reviewRepository;
    private final CatedraRepository catedraRepository;
    private final VotoUtilRepository votoUtilRepository;

    /**
     * Una página de reviews de una cátedra como view models. Por defecto
     * ordenadas por fecha (más recientes primero); con orden="utiles", por
     * cantidad de votos útiles. El orden lo aplica la base (ver repository):
     * ordenar en memoria una página ya cortada daría un ranking global roto.
     */
    public Page<ReviewView> findByCatedra(Long catedraId, String dniUsuarioActual, String orden, Pageable pageable) {
        Page<Review> pagina = ORDEN_UTILES.equals(orden)
                ? reviewRepository.findByCatedraIdOrdenVotosUtiles(catedraId, pageable)
                : reviewRepository.findByCatedraIdOrderByFechaCreacionDesc(catedraId, pageable);

        // Votos por review en una sola query, para no hacer un COUNT por cada una.
        Map<Long, Long> votosPorReview = new HashMap<>();
        for (Object[] fila : votoUtilRepository.contarPorReviewDeCatedra(catedraId)) {
            votosPorReview.put((Long) fila[0], (Long) fila[1]);
        }
        Set<Long> misVotos = dniUsuarioActual == null
                ? Set.of()
                : Set.copyOf(votoUtilRepository.reviewIdsVotadasPor(dniUsuarioActual, catedraId));

        return pagina.map(r -> new ReviewView(
                r.getId(),
                r.getUsuario().getNombre(),
                r.getPuntuacion(),
                r.getComentario(),
                r.getCuatrimestre(),
                r.getFechaCreacion().format(FORMATO_FECHA),
                dniUsuarioActual != null && dniUsuarioActual.equals(r.getUsuario().getDni()),
                votosPorReview.getOrDefault(r.getId(), 0L),
                misVotos.contains(r.getId())
        ));
    }

    /**
     * ¿El usuario ya dejó review en esta cátedra? Con la lista paginada el
     * cliente ya no puede deducirlo mirando las reviews (la propia puede caer
     * en otra página), así que se lo decimos explícitamente en el detalle.
     */
    public boolean yaReviewo(Long catedraId, String dniUsuario) {
        return dniUsuario != null && reviewRepository.existsByUsuarioDniAndCatedraId(dniUsuario, catedraId);
    }

    /**
     * Trae una review verificando que pertenezca al usuario. Si no existe o no es
     * suya, lanza excepción. Es la barrera de autorización del lado del servidor:
     * no alcanza con ocultar el botón en la vista.
     */
    public Review obtenerPropia(Long reviewId, Usuario usuario) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review no encontrada"));
        if (!review.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No tenés permiso para modificar esta review");
        }
        return review;
    }

    /** Edita una review propia. Devuelve el id de la cátedra para redirigir. */
    public Long editar(Long reviewId, Usuario usuario, Integer puntuacion, String comentario,
                       String cuatrimestre) {
        validarCuatrimestre(cuatrimestre);
        Review review = obtenerPropia(reviewId, usuario);
        review.setPuntuacion(puntuacion);
        review.setComentario(comentario);
        review.setCuatrimestre(cuatrimestre);
        reviewRepository.save(review);
        return review.getCatedra().getId();
    }

    /** Elimina una review propia. Devuelve el id de la cátedra para redirigir. */
    @Transactional
    public Long eliminar(Long reviewId, Usuario usuario) {
        Review review = obtenerPropia(reviewId, usuario);
        Long catedraId = review.getCatedra().getId();
        // Primero los votos que la referencian, si no la FK impide borrarla.
        votoUtilRepository.deleteByReviewId(reviewId);
        reviewRepository.delete(review);
        return catedraId;
    }

    /**
     * Marca/desmarca una review como útil (toggle). No se puede votar la review
     * propia; el constraint único usuario+review respalda el anti-duplicado en la
     * base. Devuelve el id de la cátedra para redirigir.
     */
    @Transactional
    public Long votarUtil(Long reviewId, Usuario usuario) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review no encontrada"));
        if (review.getUsuario().getId().equals(usuario.getId())) {
            throw new ForbiddenException("No podés votar tu propia review");
        }

        votoUtilRepository.findByUsuarioIdAndReviewId(usuario.getId(), reviewId)
                .ifPresentOrElse(
                        votoUtilRepository::delete,
                        () -> {
                            VotoUtil voto = new VotoUtil();
                            voto.setUsuario(usuario);
                            voto.setReview(review);
                            votoUtilRepository.save(voto);
                        });
        return review.getCatedra().getId();
    }

    public Review crear(Long catedraId, Usuario usuario, Integer puntuacion, String comentario,
                        String cuatrimestre) {
        validarCuatrimestre(cuatrimestre);

        // Validar que el usuario no haya hecho review de esta cátedra ya
        if (reviewRepository.existsByUsuarioIdAndCatedraId(usuario.getId(), catedraId)) {
            throw new ConflictException("Ya dejaste una review para esta cátedra");
        }

        Catedra catedra = catedraRepository.findById(catedraId)
                .orElseThrow(() -> new NotFoundException("Cátedra no encontrada"));

        Review review = new Review();
        review.setUsuario(usuario);
        review.setCatedra(catedra);
        review.setPuntuacion(puntuacion);
        review.setComentario(comentario);
        review.setCuatrimestre(cuatrimestre);
        review.setFechaCreacion(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    /**
     * El @NotBlank del DTO solo garantiza que venga algo; que sea una opción real
     * ("1C 2018" hasta el cuatrimestre en curso) se verifica acá, para que un
     * cliente que no pase por el form no pueda inventar valores.
     */
    private void validarCuatrimestre(String cuatrimestre) {
        if (!Cuatrimestre.esValido(cuatrimestre)) {
            throw new BadRequestException("El cuatrimestre no es válido");
        }
    }
}
