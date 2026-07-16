import { useState } from 'react';

/**
 * Estrellas de solo lectura. Redondea `valor` (0..5) para pintar cuántas llenas
 * y deja el resto vacías en color neutral. `size` es el font-size en px.
 */
export function Stars({ valor = 0, size = 16 }) {
  const llenas = Math.round(valor);
  return (
    <span className="stars" style={{ fontSize: size }} aria-hidden="true">
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= llenas ? '' : 'empty'}>
          ★
        </span>
      ))}
    </span>
  );
}

/**
 * Estrellas interactivas para el formulario de review. `valor` es 0..5;
 * `onChange(n)` se llama al clickear. Muestra un preview al pasar el mouse.
 */
export function StarsInput({ valor = 0, onChange }) {
  const [hover, setHover] = useState(0);
  const activo = hover || valor;

  return (
    <span className="star-input" role="radiogroup" aria-label="Puntuación">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          className={n <= activo ? 'on' : ''}
          aria-label={`${n} ${n === 1 ? 'estrella' : 'estrellas'}`}
          aria-pressed={n === valor}
          onMouseEnter={() => setHover(n)}
          onMouseLeave={() => setHover(0)}
          onClick={() => onChange(n)}
        >
          ★
        </button>
      ))}
    </span>
  );
}
