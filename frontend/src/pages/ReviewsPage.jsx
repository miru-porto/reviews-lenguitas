import { useState } from 'react';
import { useParams, Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getCatedra, getReviewsDeCatedra } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import DesgloseRating from '../components/DesgloseRating';
import ReviewCard from '../components/ReviewCard';

/**
 * La pantalla estrella: el detalle de una cátedra con el desglose de rating y la
 * lista de reviews, que se puede ordenar por fecha o por más útiles.
 *
 * Hace dos llamadas independientes:
 *  - el detalle de la cátedra (encabezado + desglose), que solo depende del id;
 *  - las reviews, que dependen del id y del orden elegido — por eso `orden` está
 *    en las dependencias de useApi: al cambiarlo se vuelve a pedir la lista.
 */
export default function ReviewsPage() {
  const { catedraId } = useParams();
  const [orden, setOrden] = useState('fecha');

  const detalle = useApi(() => getCatedra(catedraId), [catedraId]);
  const reviews = useApi(
    () => getReviewsDeCatedra(catedraId, orden),
    [catedraId, orden]
  );

  if (detalle.cargando) return <Cargando />;
  if (detalle.error) return <ErrorMensaje error={detalle.error} />;

  const { catedra, rating } = detalle.data;

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
            if (nuevo !== null) setOrden(nuevo);
          }}
        >
          <ToggleButton value="fecha">Recientes</ToggleButton>
          <ToggleButton value="utiles">Más útiles</ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {/* La lista se recarga sola al cambiar el orden; mostramos su propio
          estado de carga/error sin tapar el encabezado ya cargado. */}
      {reviews.cargando ? (
        <Cargando />
      ) : reviews.error ? (
        <ErrorMensaje error={reviews.error} />
      ) : reviews.data.length === 0 ? (
        <Vacio>Todavía no hay reviews para esta cátedra.</Vacio>
      ) : (
        <Stack spacing={2}>
          {reviews.data.map((r) => (
            <ReviewCard key={r.id} review={r} />
          ))}
        </Stack>
      )}
    </>
  );
}
