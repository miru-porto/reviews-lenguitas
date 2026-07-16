import { Stars } from './ui/Stars';

/**
 * Desglose de rating de una cátedra: el promedio grande a la izquierda y, a la
 * derecha, una barra por cada cantidad de estrellas (5 a 1) con su cantidad.
 * Recibe el objeto RatingBreakdown de la API: { promedio, total, niveles }.
 */
export default function DesgloseRating({ rating }) {
  const { promedio, total, niveles } = rating;

  if (total === 0) {
    return <p className="text-muted" style={{ margin: 0 }}>Esta cátedra todavía no tiene reviews.</p>;
  }

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'auto 1fr',
        gap: 'var(--space-8)',
        alignItems: 'center',
      }}
    >
      {/* Promedio grande */}
      <div style={{ textAlign: 'center', minWidth: 120 }}>
        <div style={{ fontFamily: 'var(--font-heading)', fontSize: 64, lineHeight: 1 }}>
          {promedio.toFixed(1)}
        </div>
        <Stars valor={promedio} size={20} />
        <div className="text-muted" style={{ fontSize: 13, marginTop: 4 }}>
          {total} {total === 1 ? 'review' : 'reviews'}
        </div>
      </div>

      {/* Barras por nivel */}
      <div>
        {niveles.map((nivel) => (
          <div
            key={nivel.estrellas}
            style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}
          >
            <span style={{ width: 28, fontSize: 13 }}>{nivel.estrellas}★</span>
            <span className="bar-track">
              <span className="bar-fill" style={{ width: `${nivel.porcentaje}%` }} />
            </span>
            <span className="text-muted" style={{ width: 28, fontSize: 12, textAlign: 'right' }}>
              {nivel.cantidad}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
