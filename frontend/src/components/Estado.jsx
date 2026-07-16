/** Spinner centrado, para mientras carga una pantalla. */
export function Cargando() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-8) 0' }}>
      <div className="spinner" role="status" aria-label="Cargando" />
    </div>
  );
}

/** Mensaje de error de la API. */
export function ErrorMensaje({ error }) {
  return (
    <div className="alert alert-error" role="alert" style={{ margin: 'var(--space-4) 0' }}>
      {error?.message || 'Ocurrió un error inesperado'}
    </div>
  );
}

/** Texto gris para listas vacías ("no hay nada todavía"). */
export function Vacio({ children }) {
  return (
    <p className="text-muted" style={{ padding: 'var(--space-8) 0', textAlign: 'center' }}>
      {children}
    </p>
  );
}
