import { useEffect } from 'react';

/**
 * Shell de modal Nocturne: backdrop + panel centrado. Cierra con Escape o click
 * en el fondo, salvo que `onClose` sea undefined (ej: mientras se envía un form).
 * El contenido (título, cuerpo, acciones con las clases .dialog-title /
 * .dialog-actions) lo pone quien lo usa, para que sirva igual con o sin <form>.
 */
export default function Dialog({ onClose, labelledBy, children }) {
  useEffect(() => {
    if (!onClose) return undefined;
    function onKey(e) {
      if (e.key === 'Escape') onClose();
    }
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div
      className="dialog-backdrop"
      onClick={onClose ? () => onClose() : undefined}
    >
      <div
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
        // Frenar la propagación: un click dentro del panel no debe cerrar.
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}
