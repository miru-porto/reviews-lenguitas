import Box from '@mui/material/Box';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import LinearProgress from '@mui/material/LinearProgress';
import Rating from '@mui/material/Rating';

/**
 * Desglose de rating de una cátedra: el promedio grande a la izquierda y, a la
 * derecha, una barra por cada cantidad de estrellas (5 a 1) con su porcentaje.
 * Recibe el objeto RatingBreakdown de la API: { promedio, total, niveles }.
 */
export default function DesgloseRating({ rating }) {
  const { promedio, total, niveles } = rating;

  if (total === 0) {
    return (
      <Typography color="text.secondary">
        Esta cátedra todavía no tiene reviews.
      </Typography>
    );
  }

  return (
    <Stack
      direction={{ xs: 'column', sm: 'row' }}
      spacing={3}
      sx={{ alignItems: 'center' }}
    >
      {/* Promedio grande */}
      <Box sx={{ textAlign: 'center', minWidth: 120 }}>
        <Typography variant="h2" component="div" fontWeight={700}>
          {promedio.toFixed(1)}
        </Typography>
        <Rating value={promedio} precision={0.1} readOnly />
        <Typography variant="body2" color="text.secondary">
          {total} {total === 1 ? 'review' : 'reviews'}
        </Typography>
      </Box>

      {/* Barras por nivel */}
      <Box sx={{ flexGrow: 1, width: '100%' }}>
        {niveles.map((nivel) => (
          <Box
            key={nivel.estrellas}
            sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}
          >
            <Typography variant="body2" sx={{ width: 24 }}>
              {nivel.estrellas}★
            </Typography>
            <LinearProgress
              variant="determinate"
              value={nivel.porcentaje}
              sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
            />
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ width: 48, textAlign: 'right' }}
            >
              {nivel.cantidad}
            </Typography>
          </Box>
        ))}
      </Box>
    </Stack>
  );
}
