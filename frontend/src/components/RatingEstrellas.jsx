import { Stars } from './ui/Stars';

/**
 * Promedio de estrellas (solo lectura) con el número al lado. `promedio` puede
 * venir null cuando la cátedra todavía no tiene reviews.
 */
export default function RatingEstrellas({ promedio, total }) {
  const valor = promedio ?? 0;

  return (
    <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
      <Stars valor={valor} size={16} />
      <span className="text-muted" style={{ fontSize: 13 }}>
        {promedio != null ? valor.toFixed(1) : 'Sin reviews'}
        {promedio != null && total != null && ` · ${total} ${total === 1 ? 'review' : 'reviews'}`}
      </span>
    </div>
  );
}
