import { Stars } from './ui/Stars';
import Avatar from './ui/Avatar';
import Tag from './ui/Tag';
import Button from './ui/Button';
import { IconThumbUp, IconEdit, IconTrash } from './ui/icons';

/**
 * Una review: autor, fecha, puntuación en estrellas, comentario y cuántos la
 * marcaron útil. Con sesión el chip "útil" se vuelve un botón que togglea el
 * voto, y la review propia muestra editar / borrar.
 *
 * Props:
 *  - review: ReviewView { id, autor, puntuacion, comentario, cuatrimestre,
 *    fecha, esMia, votosUtil, laVoteUtil }
 *  - onVotar(): si viene, el chip es clickeable (usuario logueado y review ajena).
 *  - onEditar(), onBorrar(): si vienen, se muestran los botones (review propia).
 *  - ocupado: deshabilita las acciones mientras hay una mutación en curso.
 */
export default function ReviewCard({ review, onVotar, onEditar, onBorrar, ocupado }) {
  const etiquetaUtil = (
    <>
      <IconThumbUp size={13} /> Útil · {review.votosUtil}
    </>
  );

  return (
    <div className="card elev-sm">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 'var(--space-3)' }}>
        <div style={{ display: 'flex', gap: 'var(--space-3)', alignItems: 'center' }}>
          <Avatar nombre={review.autor} size={38} variant="neutral" />
          <div>
            <div style={{ fontWeight: 600 }}>{review.autor}</div>
            <div className="text-muted" style={{ fontSize: 12 }}>
              {/* Las reviews anteriores al campo cuatrimestre no lo tienen. */}
              {review.cuatrimestre ? `${review.fecha} · Cursó en ${review.cuatrimestre}` : review.fecha}
            </div>
          </div>
        </div>
        <Stars valor={review.puntuacion} size={16} />
      </div>

      <p style={{ whiteSpace: 'pre-wrap', margin: 'var(--space-2) 0 var(--space-3)' }}>
        {review.comentario}
      </p>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
        {/* Botones de la review propia (editar/borrar), o nada. */}
        <div style={{ display: 'flex', gap: 6 }}>
          {onEditar && (
            <Button variant="ghost" icon={IconEdit} onClick={onEditar} disabled={ocupado} style={{ fontSize: 13 }}>
              Editar
            </Button>
          )}
          {onBorrar && (
            <Button variant="ghost" icon={IconTrash} onClick={onBorrar} disabled={ocupado} style={{ fontSize: 13, color: '#e5484d' }}>
              Borrar
            </Button>
          )}
        </div>

        {/* Chip "útil": clickeable si hay sesión y la review es ajena; si no,
            etiqueta de solo lectura. */}
        {onVotar ? (
          <Tag variant={review.laVoteUtil ? 'accent' : 'outline'} onClick={onVotar} disabled={ocupado}>
            {etiquetaUtil}
          </Tag>
        ) : (
          <Tag variant={review.laVoteUtil ? 'accent' : 'neutral'}>{etiquetaUtil}</Tag>
        )}
      </div>
    </div>
  );
}
