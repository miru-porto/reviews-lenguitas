import { useEffect } from 'react';

/**
 * Aviso flotante transitorio (reemplaza al Snackbar de MUI). Se muestra si
 * `mensaje` no es vacío y se autocierra a los `duracion` ms; también se puede
 * cerrar con la cruz. Se usa para errores de acciones sueltas (votar/borrar).
 */
export default function Toast({ mensaje, onCerrar, duracion = 5000 }) {
  useEffect(() => {
    if (!mensaje) return undefined;
    const t = setTimeout(onCerrar, duracion);
    return () => clearTimeout(t);
  }, [mensaje, duracion, onCerrar]);

  if (!mensaje) return null;

  return (
    <div className="toast alert alert-error" role="alert">
      <span style={{ flex: 1 }}>{mensaje}</span>
      <button
        type="button"
        className="btn btn-ghost btn-icon"
        aria-label="Cerrar"
        onClick={onCerrar}
        style={{ width: 24, height: 24, color: 'inherit' }}
      >
        ✕
      </button>
    </div>
  );
}
