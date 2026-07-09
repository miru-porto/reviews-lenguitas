import Box from '@mui/material/Box';
import Rating from '@mui/material/Rating';
import Typography from '@mui/material/Typography';

/**
 * Muestra un promedio de estrellas (solo lectura) con el número al lado.
 * `promedio` puede venir null cuando la cátedra todavía no tiene reviews.
 */
export default function RatingEstrellas({ promedio, total }) {
  const valor = promedio ?? 0;

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Rating value={valor} precision={0.1} readOnly size="small" />
      <Typography variant="body2" color="text.secondary">
        {promedio != null ? valor.toFixed(1) : 'Sin reviews'}
        {total != null && ` (${total})`}
      </Typography>
    </Box>
  );
}
