import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardActions from '@mui/material/CardActions';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Rating from '@mui/material/Rating';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import ThumbUpOutlinedIcon from '@mui/icons-material/ThumbUpOutlined';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

/**
 * Una review: autor, fecha, puntuación en estrellas, comentario y cuántos la
 * marcaron útil. Con sesión (Fase 4c) el chip "útil" se vuelve un botón que
 * togglea el voto, y la review propia muestra editar / borrar.
 *
 * Props:
 *  - review: ReviewView { id, autor, puntuacion, comentario, cuatrimestre,
 *    fecha, esMia, votosUtil, laVoteUtil }
 *  - onVotar(): si viene, el chip es clickeable (usuario logueado y review ajena).
 *  - onEditar(), onBorrar(): si vienen, se muestran los botones (review propia).
 *  - ocupado: deshabilita las acciones mientras hay una mutación en curso.
 */
export default function ReviewCard({ review, onVotar, onEditar, onBorrar, ocupado }) {
  const chipUtil = (
    <Chip
      icon={<ThumbUpOutlinedIcon />}
      label={`Útil · ${review.votosUtil}`}
      size="small"
      variant={review.laVoteUtil ? 'filled' : 'outlined'}
      color={review.laVoteUtil ? 'primary' : 'default'}
      onClick={onVotar}
      disabled={ocupado}
      // clickable lo maneja MUI según si hay onClick; con onClick undefined queda
      // como una etiqueta de solo lectura (deslogueado o review propia).
    />
  );

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
              {/* Las reviews anteriores al campo cuatrimestre no lo tienen. */}
              {review.cuatrimestre ? `${review.fecha} · Cursó en ${review.cuatrimestre}` : review.fecha}
            </Typography>
          </Box>
          <Rating value={review.puntuacion} readOnly size="small" />
        </Box>

        <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', mb: 1.5 }}>
          {review.comentario}
        </Typography>

        {chipUtil}
      </CardContent>

      {review.esMia && (onEditar || onBorrar) && (
        <CardActions sx={{ px: 2, pb: 2, pt: 0 }}>
          {onEditar && (
            <Button
              size="small"
              startIcon={<EditIcon />}
              onClick={onEditar}
              disabled={ocupado}
            >
              Editar
            </Button>
          )}
          {onBorrar && (
            <Button
              size="small"
              color="error"
              startIcon={<DeleteIcon />}
              onClick={onBorrar}
              disabled={ocupado}
            >
              Borrar
            </Button>
          )}
        </CardActions>
      )}
    </Card>
  );
}
