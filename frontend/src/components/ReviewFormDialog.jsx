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
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import { crearReview, editarReview } from '../api/api';
import { opcionesCuatrimestre } from '../utils/cuatrimestres';

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
 *  - review: { id, puntuacion, comentario, cuatrimestre } requerido en modo 'editar'
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
  // Las reviews anteriores al campo no tienen cuatrimestre (null): al editarlas
  // arranca sin selección y hay que elegirlo para poder guardar.
  const [cuatrimestre, setCuatrimestre] = useState(esEditar ? (review.cuatrimestre ?? '') : '');
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
    if (!cuatrimestre) {
      setError('Elegí el cuatrimestre que cursaste.');
      return;
    }
    if (comentario.trim() === '') {
      setError('El comentario no puede estar vacío.');
      return;
    }

    setEnviando(true);
    try {
      if (esEditar) {
        await editarReview(review.id, puntuacion, comentario.trim(), cuatrimestre);
      } else {
        await crearReview(catedraId, puntuacion, comentario.trim(), cuatrimestre);
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

          <Typography component="legend" sx={{ mb: 0.5 }}>
            Cuatrimestre cursado
          </Typography>
          <Select
            value={cuatrimestre}
            onChange={(e) => setCuatrimestre(e.target.value)}
            fullWidth
            // Select no tiene placeholder nativo: displayEmpty hace que con valor
            // vacío se llame igual a renderValue, y ahí mostramos el texto gris.
            displayEmpty
            renderValue={(valor) =>
              valor || (
                <Typography component="span" color="text.secondary">
                  Seleccioná el cuatrimestre
                </Typography>
              )
            }
            // Son ~18 opciones (desde 1C 2018): limitamos el alto del menú para
            // que no ocupe toda la pantalla y se scrollee como una lista.
            MenuProps={{ slotProps: { paper: { sx: { maxHeight: 320 } } } }}
            sx={{ mb: 2 }}
          >
            {opcionesCuatrimestre().map((c) => (
              <MenuItem key={c} value={c}>
                {c}
              </MenuItem>
            ))}
          </Select>

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
