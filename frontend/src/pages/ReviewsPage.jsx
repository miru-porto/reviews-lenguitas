import { useState, useEffect } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Pagination from '@mui/material/Pagination';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import RateReviewIcon from '@mui/icons-material/RateReview';
import { getCatedra, getReviewsDeCatedra, votarUtil, borrarReview } from '../api/api';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../auth/AuthContext';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import DesgloseRating from '../components/DesgloseRating';
import ReviewCard from '../components/ReviewCard';
import ReviewFormDialog from '../components/ReviewFormDialog';

/**
 * La pantalla estrella: el detalle de una cátedra con el desglose de rating y la
 * lista de reviews, que se puede ordenar por fecha o por más útiles.
 *
 * En Fase 4c esta pantalla pasa de solo lectura a interactiva para el usuario
 * logueado: escribir su review, editar/borrar la propia y votar útil las ajenas.
 * Todas las mutaciones recargan detalle + lista para reflejar el nuevo estado
 * (rating, conteos, botones), y sus errores (400/401/403/409) se muestran en
 * pantalla — en el diálogo del formulario o en un Snackbar para las acciones sueltas.
 */
export default function ReviewsPage() {
  const { catedraId } = useParams();
  const { usuario } = useAuth();
  const [orden, setOrden] = useState('fecha');
  // Página actual (0-based, como la pide la API). Cambiar de orden o de
  // cátedra vuelve a la primera: la página N de un orden no significa nada
  // en el otro.
  const [pagina, setPagina] = useState(0);

  const detalle = useApi(() => getCatedra(catedraId), [catedraId]);
  const reviews = useApi(
    () => getReviewsDeCatedra(catedraId, orden, pagina),
    [catedraId, orden, pagina]
  );

  // La respuesta ahora es una página: las reviews en content y los metadatos
  // (total de páginas, número actual) en page.
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

  // Al navegar a otra cátedra el componente no se remonta (cambia solo el
  // parámetro de la ruta): hay que volver a la primera página a mano.
  useEffect(() => {
    setPagina(0);
  }, [catedraId]);

  // Diálogo de formulario: null = cerrado; { modo:'crear' } o { modo:'editar', review }.
  const [formulario, setFormulario] = useState(null);
  // Review pendiente de confirmación de borrado (o null).
  const [aBorrar, setABorrar] = useState(null);
  // true mientras corre una mutación suelta (votar/borrar): desactiva sus botones.
  const [ocupado, setOcupado] = useState(false);
  // Mensaje de error transitorio para acciones fuera del formulario.
  const [errorAccion, setErrorAccion] = useState('');

  if (detalle.cargando) return <Cargando />;
  if (detalle.error) return <ErrorMensaje error={detalle.error} />;

  // yaReviewe lo calcula el backend: con la lista paginada no alcanza con mirar
  // la página actual para saber si mi review existe (puede estar en otra página).
  const { catedra, rating, yaReviewe } = detalle.data;

  /** Recarga lista + desglose tras una mutación (cambia conteos y promedio). */
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
      <Button
        component={RouterLink}
        to={`/materias/${catedra.materiaId}`}
        startIcon={<ArrowBackIcon />}
        sx={{ mb: 2 }}
      >
        {catedra.materiaNombre}
      </Button>

      <Typography variant="h4" gutterBottom>
        {catedra.nombreProfesor} {catedra.apellidoProfesor}
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" gutterBottom>
        {catedra.materiaNombre}
      </Typography>

      <Paper sx={{ p: 3, my: 3 }}>
        <DesgloseRating rating={rating} />
      </Paper>

      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 2,
        }}
      >
        <Typography variant="h5">Reviews</Typography>
        <ToggleButtonGroup
          value={orden}
          exclusive
          size="small"
          onChange={(e, nuevo) => {
            // El toggle devuelve null si se clickea el botón ya activo; lo
            // ignoramos para que siempre quede un orden seleccionado.
            if (nuevo !== null) {
              setOrden(nuevo);
              setPagina(0);
            }
          }}
        >
          <ToggleButton value="fecha">Recientes</ToggleButton>
          <ToggleButton value="utiles">Más útiles</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {/* Llamada a la acción según sesión: escribir review, o invitar a ingresar.
          Si el usuario ya reseñó esta cátedra no mostramos nada. */}
      {usuario ? (
        !yaReviewe && (
          <Button
            variant="contained"
            startIcon={<RateReviewIcon />}
            onClick={() => setFormulario({ modo: 'crear' })}
            sx={{ mb: 2 }}
          >
            Escribir una review
          </Button>
        )
      ) : (
        <Button component={RouterLink} to="/login" sx={{ mb: 2 }}>
          Ingresá para dejar tu review
        </Button>
      )}

      {/* La lista se recarga sola al cambiar el orden; mostramos su propio
          estado de carga/error sin tapar el encabezado ya cargado. */}
      {reviews.cargando ? (
        <Cargando />
      ) : reviews.error ? (
        <ErrorMensaje error={reviews.error} />
      ) : listaReviews.length === 0 ? (
        <Vacio>Todavía no hay reviews para esta cátedra.</Vacio>
      ) : (
        <>
          <Stack spacing={2}>
            {listaReviews.map((r) => (
              <ReviewCard
                key={r.id}
                review={r}
                ocupado={ocupado}
                // El chip vota solo si hay sesión y la review es ajena.
                onVotar={usuario && !r.esMia ? () => votar(r.id) : undefined}
                onEditar={r.esMia ? () => setFormulario({ modo: 'editar', review: r }) : undefined}
                onBorrar={r.esMia ? () => setABorrar(r) : undefined}
              />
            ))}
          </Stack>

          {/* Paginador: solo aparece si hay más de una página. MUI cuenta las
              páginas desde 1; la API desde 0, de ahí el +1/-1. */}
          {totalPaginas > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
              <Pagination
                count={totalPaginas}
                page={pagina + 1}
                onChange={(e, nueva) => setPagina(nueva - 1)}
                color="primary"
              />
            </Box>
          )}
        </>
      )}

      {/* Formulario de crear / editar review. */}
      {formulario && (
        <ReviewFormDialog
          open
          modo={formulario.modo}
          catedraId={catedraId}
          review={formulario.review}
          onClose={() => setFormulario(null)}
          onGuardado={recargarTodo}
        />
      )}

      {/* Confirmación de borrado. */}
      <Dialog open={aBorrar !== null} onClose={() => setABorrar(null)}>
        <DialogTitle>Borrar review</DialogTitle>
        <DialogContent>
          <DialogContentText>
            ¿Seguro que querés borrar tu review? Esta acción no se puede deshacer.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setABorrar(null)}>Cancelar</Button>
          <Button color="error" onClick={confirmarBorrado}>
            Borrar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Errores de acciones sueltas (votar / borrar). */}
      <Snackbar
        open={errorAccion !== ''}
        autoHideDuration={5000}
        onClose={() => setErrorAccion('')}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="error" onClose={() => setErrorAccion('')}>
          {errorAccion}
        </Alert>
      </Snackbar>
    </>
  );
}
