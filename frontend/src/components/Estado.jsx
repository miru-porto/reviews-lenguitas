import { useEffect, useState } from 'react';

/**
 * Spinner centrado, para mientras carga una pantalla.
 *
 * Si la espera se estira deja de ser un spinner mudo y explica qué pasa: el
 * backend vive en el plan gratuito de Render, que se duerme tras ~15 minutos sin
 * tráfico y tarda hasta un minuto en despertar. Sin este aviso la app parece
 * rota justo cuando está funcionando — el front lo sirve Vercel y aparece al
 * instante, así que la pantalla se ve viva mientras la data no llega.
 *
 * El texto es condicional ("si estuvo un rato sin uso") a propósito: desde acá
 * no hay forma de distinguir un cold start de una consulta lenta, y afirmar la
 * causa equivocada sería mentirle a quien está esperando.
 */
export function Cargando({ demoraMs = 4000 }) {
  const [lento, setLento] = useState(false);

  useEffect(() => {
    const t = setTimeout(() => setLento(true), demoraMs);
    return () => clearTimeout(t);
  }, [demoraMs]);

  return (
    <div
      style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        gap: 'var(--space-4)', padding: 'var(--space-8) 0',
      }}
    >
      <div className="spinner" role="status" aria-label="Cargando" />

      {lento && (
        // aria-live: quien usa lector de pantalla se entera del cambio sin
        // tener que ir a buscarlo.
        <div aria-live="polite" style={{ maxWidth: 380, textAlign: 'center' }}>
          <p style={{ margin: 0, marginBottom: 4 }}>Despertando el servidor…</p>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Tarda más la primera vez: está en un plan gratuito y se duerme si
            estuvo un rato sin uso. Puede demorar hasta un minuto. Después anda
            normal.
          </p>
        </div>
      )}
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
