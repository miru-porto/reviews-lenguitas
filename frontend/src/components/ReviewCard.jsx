import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Rating from '@mui/material/Rating';
import Chip from '@mui/material/Chip';
import ThumbUpOutlinedIcon from '@mui/icons-material/ThumbUpOutlined';

/**
 * Una review: autor, fecha, puntuación en estrellas, comentario y cuántos la
 * marcaron útil. En Fase 3 el conteo de útiles es solo lectura; el botón para
 * votar (que necesita sesión) llega en Fase 4.
 */
export default function ReviewCard({ review }) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 1,
          }}
        >
          <Box>
            <Typography variant="subtitle1" fontWeight={600}>
              {review.autor}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {review.fecha}
            </Typography>
          </Box>
          <Rating value={review.puntuacion} readOnly size="small" />
        </Box>

        <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', mb: 1.5 }}>
          {review.comentario}
        </Typography>

        <Chip
          icon={<ThumbUpOutlinedIcon />}
          label={`Útil · ${review.votosUtil}`}
          size="small"
          variant="outlined"
          color={review.laVoteUtil ? 'primary' : 'default'}
        />
      </CardContent>
    </Card>
  );
}
