import { useState } from 'react';
import { crearReview, editarReview } from '../api/api';
import { opcionesCuatrimestre } from '../utils/cuatrimestres';
import Dialog from './ui/Dialog';
import Button from './ui/Button';
import { Field, Select, Textarea } from './ui/Field';
import { StarsInput } from './ui/Stars';

const MAX_COMENTARIO = 2000;

/**
 * Diálogo para escribir o editar una review. El mismo formulario sirve para los
 * dos casos según `modo`:
 *  - 'crear': arranca vacío y hace POST /api/reviews con la catedraId.
 *  - 'editar': viene precargado con la review y hace PUT /api/reviews/{id}.
 *
 * Al guardar con éxito llama a onGuardado() (la pantalla recarga la lista) y
 * cierra. Los errores del backend (400 validación, 409 duplicada, 403/404) se
 * muestran en un alert dentro del diálogo, sin cerrarlo, para poder corregir.
 */
export default function ReviewFormDialog({ onClose, onGuardado, modo, catedraId, review }) {
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

    // Validación local, en espejo de las constraints del backend.
    if (!puntuacion) return setError('Elegí una puntuación.');
    if (!cuatrimestre) return setError('Elegí el cuatrimestre que cursaste.');
    if (comentario.trim() === '') return setError('El comentario no puede estar vacío.');

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
    <Dialog onClose={enviando ? undefined : onClose} labelledBy="review-form-title">
      <form onSubmit={guardar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        <div className="dialog-title" id="review-form-title">
          {esEditar ? 'Editar review' : 'Escribir una review'}
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <Field label="Tu puntuación">
          <StarsInput valor={puntuacion} onChange={setPuntuacion} />
        </Field>

        <Field label="Cuatrimestre cursado" htmlFor="cuatri">
          <Select
            id="cuatri"
            value={cuatrimestre}
            onChange={(e) => setCuatrimestre(e.target.value)}
            placeholder="Seleccioná el cuatrimestre"
          >
            {opcionesCuatrimestre().map((c) => (
              <option key={c} value={c}>{c}</option>
            ))}
          </Select>
        </Field>

        <Field label="Comentario" htmlFor="comentario">
          <Textarea
            id="comentario"
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            rows={4}
            maxLength={MAX_COMENTARIO}
            autoFocus
            placeholder="Contá cómo fue cursarla: exigencia, correcciones, clases…"
          />
          <div className="text-muted" style={{ fontSize: 11, marginTop: 4, textAlign: 'right' }}>
            {comentario.length} / {MAX_COMENTARIO}
          </div>
        </Field>

        <div className="dialog-actions">
          <Button variant="secondary" onClick={onClose} disabled={enviando}>Cancelar</Button>
          <Button variant="primary" type="submit" disabled={enviando}>
            {esEditar ? 'Guardar cambios' : 'Publicar'}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
