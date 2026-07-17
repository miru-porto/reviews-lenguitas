import { useEffect, useState } from 'react';
import { otroDato } from './datosCuriosos';

/** Cada cuánto rota el dato curioso. Suficiente para leerlo sin apuro. */
const ROTACION_MS = 7000;

/**
 * Spinner centrado, para mientras carga una pantalla.
 *
 * Si la espera se estira deja de ser un spinner mudo: explica qué pasa y da algo
 * para leer. El backend vive en el plan gratuito de Render, que se duerme tras
 * ~15 minutos sin tráfico y tarda hasta un minuto en despertar. Sin esto la app
 * parece rota justo cuando está funcionando — el front lo sirve Vercel y aparece
 * al instante, así que la pantalla se ve viva mientras la data no llega.
 *
 * El texto es condicional ("si estuvo un rato sin uso") a propósito: desde acá
 * no hay forma de distinguir un cold start de una consulta lenta, y afirmar la
 * causa equivocada sería mentirle a quien está esperando.
 */
export function Cargando({ demoraMs = 4000 }) {
  const [lento, setLento] = useState(false);
  const [dato, setDato] = useState(() => otroDato(null));

  useEffect(() => {
    const t = setTimeout(() => setLento(true), demoraMs);
    return () => clearTimeout(t);
  }, [demoraMs]);

  // El intervalo arranca recién con la espera larga: en una carga normal el
  // componente se desmonta antes y no hay nada que rotar.
  useEffect(() => {
    if (!lento) return undefined;
    const i = setInterval(() => setDato(otroDato), ROTACION_MS);
    return () => clearInterval(i);
  }, [lento]);

  return (
    <div
      style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        gap: 'var(--space-4)', padding: 'var(--space-8) 0',
      }}
    >
      {/* Dormido mostramos las "z"; despierto, el spinner. Un spinner acá
          diría "estoy trabajando", y lo cierto es que está durmiendo. */}
      {lento ? (
        <div className="zzz" role="status" aria-label="Despertando el servidor">
          <span aria-hidden="true">z</span>
          <span aria-hidden="true">z</span>
          <span aria-hidden="true">z</span>
        </div>
      ) : (
        <div className="spinner" role="status" aria-label="Cargando" />
      )}

      {lento && (
        // aria-live: quien usa lector de pantalla se entera del cambio sin
        // tener que ir a buscarlo.
        <div aria-live="polite" style={{ maxWidth: 420, textAlign: 'center' }}>
          <p style={{ margin: 0, marginBottom: 4 }}>Despertando el servidor…</p>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Tarda más la primera vez: está en un plan gratuito y se duerme si
            estuvo un rato sin uso. Puede demorar hasta un minuto. Después anda
            normal.
          </p>

          <div
            style={{
              marginTop: 'var(--space-5)', paddingTop: 'var(--space-4)',
              borderTop: '1px solid var(--color-divider)',
            }}
          >
            <p className="text-muted" style={{ fontSize: 11, letterSpacing: '.06em', textTransform: 'uppercase', margin: 0, marginBottom: 6 }}>
              Mientras tanto
            </p>
            {/* La key fuerza el remount en cada rotación: sin eso React reusa el
                nodo y el fade-in no se vuelve a disparar. */}
            <p key={dato} className="dato-curioso" style={{ fontSize: 14, margin: 0 }}>
              {dato}
            </p>
          </div>
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
