/**
 * Avatar circular con las iniciales de un nombre. `nombre` puede ser el nombre
 * completo o un apellido; toma hasta dos iniciales. `variant`: accent | neutral.
 */
export default function Avatar({ nombre = '', size = 40, variant = 'neutral' }) {
  const iniciales = nombre
    .trim()
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');

  return (
    <span
      className={`avatar avatar-${variant}`}
      style={{ width: size, height: size, fontSize: size * 0.4 }}
      aria-hidden="true"
    >
      {iniciales || '?'}
    </span>
  );
}
