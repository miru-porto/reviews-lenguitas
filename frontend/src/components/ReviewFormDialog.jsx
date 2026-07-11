import { useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Rating from '@mui/material/Rating';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Alert from '@mui/material/Alert';
import { crearReview, editarReview } from '../api/api';

const MAX_COMENTARIO = 2000;

/**
 * Diálogo para escribir o editar una review. El mismo formulario sirve para los
 * dos casos según `modo`:
 *  - 'crear': arranca vacío y hace POST /api/reviews con la catedraId.
 *  - 'editar': viene precargado con la review y hace PUT /api/reviews/{id}.
 *
 * Al guardar con éxito llama a onGuardado() (la pantalla recarga la lista) y cierra.
 * Los errores del backend (400 validación, 409 review duplicada, 403/404) se
 * muestran en un Alert dentro del diálogo, sin cerrarlo, para poder corregir.
 *
 * Props:
 *  - open, onClose, onGuardado
 *  - modo: 'crear' | 'editar'
 *  - catedraId: requerido en modo 'crear'
 *  - review: { id, puntuacion, comentario } requerido en modo 'editar'
 */
export default function ReviewFormDialog({
  open,
  onClose,
  onGuardado,
  modo,
  catedraId,
  review,
}) {
  const esEditar = modo === 'editar';

  const [puntuacion, setPuntuacion] = useState(esEditar ? review.puntuacion : 0);
  const [comentario, setComentario] = useState(esEditar ? review.comentario : '');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function guardar(e) {
    e.preventDefault();
    setError('');

    // Validación local, en espejo de las constraints del backend, para dar el
    // mensaje sin ir al server.
    if (!puntuacion) {
      setError('Elegí una puntuación.');
      return;
    }
    if (comentario.trim() === '') {
      setError('El comentario no puede estar vacío.');
      return;
    }

    setEnviando(true);
    try {
      if (esEditar) {
        await editarReview(review.id, puntuacion, comentario.trim());
      } else {
        await crearReview(catedraId, puntuacion, comentario.trim());
      }
      onGuardado();
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Dialog open={open} onClose={enviando ? undefined : onClose} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={guardar}>
        <DialogTitle>{esEditar ? 'Editar review' : 'Escribir una review'}</DialogTitle>
        <DialogContent>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Typography component="legend" sx={{ mb: 0.5 }}>
            Puntuación
          </Typography>
          <Rating
            value={puntuacion}
            onChange={(e, valor) => setPuntuacion(valor ?? 0)}
            sx={{ mb: 2 }}
          />

          <TextField
            label="Comentario"
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            multiline
            minRows={4}
            fullWidth
            autoFocus
            slotProps={{ htmlInput: { maxLength: MAX_COMENTARIO } }}
            helperText={`${comentario.length}/${MAX_COMENTARIO}`}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={enviando}>
            Cancelar
          </Button>
          <Button type="submit" variant="contained" disabled={enviando}>
            {esEditar ? 'Guardar cambios' : 'Publicar'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  );
}
