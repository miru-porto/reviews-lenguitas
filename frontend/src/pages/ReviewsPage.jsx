import { useState, useEffect } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import { getCatedra, getReviewsDeCatedra, votarUtil, borrarReview } from '../api/api';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../auth/AuthContext';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import DesgloseRating from '../components/DesgloseRating';
import ReviewCard from '../components/ReviewCard';
import ReviewFormDialog from '../components/ReviewFormDialog';
import Button from '../components/ui/Button';
import Avatar from '../components/ui/Avatar';
import Segmented from '../components/ui/Segmented';
import Dialog from '../components/ui/Dialog';
import Toast from '../components/ui/Toast';
import Pager from '../components/ui/Pager';
import { IconArrowLeft, IconPlus } from '../components/ui/icons';

const OPCIONES_ORDEN = [
  { value: 'fecha', label: 'Recientes' },
  { value: 'utiles', label: 'Más útiles' },
];

/**
 * La pantalla estrella: el detalle de una cátedra con el desglose de rating y la
 * lista de reviews, que se puede ordenar por fecha o por más útiles. Con sesión,
 * el usuario puede escribir su review, editar/borrar la propia y votar útil las
 * ajenas. Cada mutación recarga detalle + lista para reflejar el nuevo estado.
 */
export default function ReviewsPage() {
  const { catedraId } = useParams();
  const { usuario } = useAuth();
  const [orden, setOrden] = useState('fecha');
  const [pagina, setPagina] = useState(0);

  const detalle = useApi(() => getCatedra(catedraId), [catedraId]);
  const reviews = useApi(
    () => getReviewsDeCatedra(catedraId, orden, pagina),
    [catedraId, orden, pagina]
  );

  const listaReviews = reviews.data?.content ?? [];
  const totalPaginas = reviews.data?.page?.totalPages ?? 0;

  // Si la página quedó fuera de rango (ej: borré la única review de la última
  // página), retrocedemos a la última que existe.
  useEffect(() => {
    if (reviews.data && listaReviews.length === 0 && pagina > 0) {
      setPagina(Math.max(0, totalPaginas - 1));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reviews.data]);

  // Al navegar a otra cátedra el componente no se remonta: reseteamos la página.
  useEffect(() => {
    setPagina(0);
  }, [catedraId]);

  const [formulario, setFormulario] = useState(null); // null | {modo:'crear'} | {modo:'editar', review}
  const [aBorrar, setABorrar] = useState(null);
  const [ocupado, setOcupado] = useState(false);
  const [errorAccion, setErrorAccion] = useState('');

  if (detalle.cargando) return <Cargando />;
  if (detalle.error) return <ErrorMensaje error={detalle.error} />;

  const { catedra, rating, yaReviewe } = detalle.data;
  const nombreProfe = `${catedra.nombreProfesor} ${catedra.apellidoProfesor}`.trim();

  function recargarTodo() {
    reviews.recargar();
    detalle.recargar();
  }

  async function votar(id) {
    setErrorAccion('');
    setOcupado(true);
    try {
      await votarUtil(id);
      recargarTodo();
    } catch (err) {
      setErrorAccion(err.message);
    } finally {
      setOcupado(false);
    }
  }

  async function confirmarBorrado() {
    const id = aBorrar.id;
    setABorrar(null);
    setErrorAccion('');
    setOcupado(true);
    try {
      await borrarReview(id);
      recargarTodo();
    } catch (err) {
      setErrorAccion(err.message);
    } finally {
      setOcupado(false);
    }
  }

  return (
    <>
      <Button as={RouterLink} to={`/materias/${catedra.materiaId}`} variant="ghost" icon={IconArrowLeft} style={{ marginBottom: 'var(--space-4)' }}>
        {catedra.materiaNombre}
      </Button>

      {/* Header: avatar + nombre + materia */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', marginBottom: 'var(--space-4)', flexWrap: 'wrap' }}>
        <Avatar nombre={catedra.apellidoProfesor} size={64} variant="accent" />
        <div>
          <h2 style={{ fontSize: 34, margin: 0 }}>{nombreProfe}</h2>
          <p className="text-muted" style={{ margin: 0 }}>{catedra.materiaNombre}</p>
        </div>
      </div>

      {/* Resumen de rating */}
      <div className="card elev-sm" style={{ padding: 'var(--space-6)', marginBottom: 'var(--space-6)' }}>
        <DesgloseRating rating={rating} />
      </div>

      {/* Encabezado de la lista: título + orden + CTA */}
      <div className="row-between" style={{ marginBottom: 'var(--space-4)' }}>
        <h3 style={{ margin: 0 }}>
          {rating.total} {rating.total === 1 ? 'review' : 'reviews'}
        </h3>
        <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center', flexWrap: 'wrap' }}>
          <Segmented
            opciones={OPCIONES_ORDEN}
            value={orden}
            onChange={(v) => { setOrden(v); setPagina(0); }}
          />
          {usuario
            ? !yaReviewe && (
                <Button variant="primary" icon={IconPlus} onClick={() => setFormulario({ modo: 'crear' })}>
                  Escribir review
                </Button>
              )
            : (
                <Button as={RouterLink} to="/login" variant="secondary">
                  Ingresá para opinar
                </Button>
              )}
        </div>
      </div>

      {/* Lista de reviews. Se recarga sola al cambiar el orden. */}
      {reviews.cargando ? (
        <Cargando />
      ) : reviews.error ? (
        <ErrorMensaje error={reviews.error} />
      ) : listaReviews.length === 0 ? (
        <Vacio>Todavía no hay reviews para esta cátedra.</Vacio>
      ) : (
        <>
          <div className="stack">
            {listaReviews.map((r) => (
              <ReviewCard
                key={r.id}
                review={r}
                ocupado={ocupado}
                onVotar={usuario && !r.esMia ? () => votar(r.id) : undefined}
                onEditar={r.esMia ? () => setFormulario({ modo: 'editar', review: r }) : undefined}
                onBorrar={r.esMia ? () => setABorrar(r) : undefined}
              />
            ))}
          </div>
          <Pager pagina={pagina} total={totalPaginas} onCambiar={setPagina} />
        </>
      )}

      {/* Formulario de crear / editar review. */}
      {formulario && (
        <ReviewFormDialog
          modo={formulario.modo}
          catedraId={catedraId}
          review={formulario.review}
          onClose={() => setFormulario(null)}
          onGuardado={recargarTodo}
        />
      )}

      {/* Confirmación de borrado. */}
      {aBorrar && (
        <Dialog onClose={() => setABorrar(null)} labelledBy="borrar-title">
          <div className="dialog-title" id="borrar-title">Borrar review</div>
          <p className="text-muted" style={{ margin: 0 }}>
            ¿Seguro que querés borrar tu review? Esta acción no se puede deshacer.
          </p>
          <div className="dialog-actions">
            <Button variant="secondary" onClick={() => setABorrar(null)}>Cancelar</Button>
            <Button variant="danger" onClick={confirmarBorrado}>Borrar</Button>
          </div>
        </Dialog>
      )}

      {/* Errores de acciones sueltas (votar / borrar). */}
      <Toast mensaje={errorAccion} onCerrar={() => setErrorAccion('')} />
    </>
  );
}
