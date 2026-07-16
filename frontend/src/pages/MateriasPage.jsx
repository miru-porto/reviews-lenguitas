import { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { getMaterias } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import { IconSearch } from '../components/ui/icons';

const NOMBRE_ANIO = {
  1: '1er año',
  2: '2do año',
  3: '3er año',
  4: '4to año',
  5: '5to año (plan de 5 años)',
};

/**
 * Abrevia los totales de la cabecera: 1900 → "1.9k". Debajo de mil se muestra
 * el número tal cual. Sin el redondeo, "1900 reviews" desbalancea la fila de
 * tres números.
 */
function compacto(n) {
  if (n < 1000) return String(n);
  const miles = n / 1000;
  // 12.4k no aporta nada sobre 12k, pero 1.9k sí sobre 2k.
  return (miles < 10 ? miles.toFixed(1).replace(/\.0$/, '') : Math.round(miles)) + 'k';
}

/** Un número grande con su etiqueta abajo (materias / cátedras / reviews). */
function Stat({ valor, label }) {
  return (
    <div>
      <div style={{ fontFamily: 'var(--font-heading)', fontSize: 30, lineHeight: 1 }}>{valor}</div>
      <div className="text-muted" style={{ fontSize: 12 }}>{label}</div>
    </div>
  );
}

/**
 * Línea de stats de una materia: "★ 4.7 · 4 cátedras · 88 reviews". Sin
 * reviews no hay promedio que mostrar (sería un 0 engañoso), así que se invita
 * a estrenar la materia.
 */
function MetaMateria({ materia }) {
  const { promedioRating, cantidadCatedras, cantidadReviews } = materia;

  if (cantidadReviews === 0) {
    return (
      <div className="card-meta">
        {cantidadCatedras === 0
          ? 'Todavía sin cátedras cargadas'
          : `${cantidadCatedras} ${cantidadCatedras === 1 ? 'cátedra' : 'cátedras'} · sin reviews todavía`}
      </div>
    );
  }

  return (
    <div className="card-meta">
      <span style={{ color: 'var(--color-accent)' }} aria-hidden="true">★</span>
      {promedioRating.toFixed(1)} · {cantidadCatedras} {cantidadCatedras === 1 ? 'cátedra' : 'cátedras'} ·{' '}
      {cantidadReviews} {cantidadReviews === 1 ? 'review' : 'reviews'}
    </div>
  );
}

/**
 * Pantalla inicial: un hero con buscador y las materias agrupadas por año de
 * cursada (1ro a 5to, según el plan de estudios). La API ya las manda ordenadas
 * por año y nombre; acá solo se cortan en secciones. Las materias sin año caen
 * en una sección "Sin año asignado" al final.
 */
export default function MateriasPage() {
  const { data: materias, cargando, error } = useApi(getMaterias, []);
  const navigate = useNavigate();
  const [texto, setTexto] = useState('');

  function buscar(e) {
    e.preventDefault();
    const q = texto.trim();
    if (q) navigate(`/buscar?q=${encodeURIComponent(q)}`);
  }

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  // Agrupar preservando el orden que ya trae la API (año, nombre). La clave
  // 'sin' junta las materias con anio null.
  const grupos = new Map();
  for (const materia of materias) {
    const clave = materia.anio ?? 'sin';
    if (!grupos.has(clave)) grupos.set(clave, []);
    grupos.get(clave).push(materia);
  }

  // Los totales se suman de la misma lista en vez de pedirlos aparte: cada
  // cátedra pertenece a una sola materia y cada review a una sola cátedra, así
  // que la suma da el total exacto y nunca contradice lo que se ve abajo.
  const totalCatedras = materias.reduce((acc, m) => acc + m.cantidadCatedras, 0);
  const totalReviews = materias.reduce((acc, m) => acc + m.cantidadReviews, 0);

  return (
    <>
      {/* Hero */}
      <section style={{ textAlign: 'center', padding: 'var(--space-8) 0 var(--space-6)' }}>
        <h1 style={{ fontSize: 46, fontWeight: 400, marginBottom: 'var(--space-2)' }}>
          Encontrá tu cátedra ideal
        </h1>
        <p className="text-muted" style={{ fontSize: 16, maxWidth: 540, margin: '0 auto var(--space-4)' }}>
          Reviews reales de estudiantes del Profesorado de Inglés, cátedra por cátedra.
          La data que te hubiera gustado tener antes de anotarte.
        </p>
        <form
          onSubmit={buscar}
          className="input elev-sm"
          style={{ display: 'flex', alignItems: 'center', gap: 10, maxWidth: 540, margin: '0 auto', minHeight: 52, fontSize: 16 }}
        >
          <IconSearch size={20} />
          <input
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Probá “Gramática”, “Del Regno”, “Fonología”…"
            aria-label="Buscar materia o profesor"
            style={{ border: 0, background: 'transparent', color: 'inherit', outline: 'none', width: '100%', fontSize: 16 }}
          />
        </form>

        {materias.length > 0 && (
          <div style={{ display: 'flex', gap: 'var(--space-6)', justifyContent: 'center', marginTop: 'var(--space-6)' }}>
            <Stat valor={compacto(materias.length)} label={materias.length === 1 ? 'materia' : 'materias'} />
            <div style={{ width: 1, background: 'var(--color-divider)' }} />
            <Stat valor={compacto(totalCatedras)} label={totalCatedras === 1 ? 'cátedra' : 'cátedras'} />
            <div style={{ width: 1, background: 'var(--color-divider)' }} />
            <Stat valor={compacto(totalReviews)} label={totalReviews === 1 ? 'review' : 'reviews'} />
          </div>
        )}
      </section>

      {materias.length === 0 ? (
        <Vacio>No hay materias cargadas.</Vacio>
      ) : (
        [...grupos.entries()].map(([anio, lista]) => (
          <section key={anio} style={{ marginBottom: 'var(--space-8)' }}>
            <div className="row-between" style={{ marginBottom: 'var(--space-3)' }}>
              <h2 style={{ margin: 0 }}>{NOMBRE_ANIO[anio] ?? 'Sin año asignado'}</h2>
              <span className="text-muted" style={{ fontSize: 13 }}>
                {lista.length} {lista.length === 1 ? 'materia' : 'materias'}
              </span>
            </div>
            <div className="section-grid">
              {lista.map((m) => (
                <RouterLink key={m.id} to={`/materias/${m.id}`} className="card card-hover elev-sm">
                  <span className="card-title">{m.nombre}</span>
                  <MetaMateria materia={m} />
                </RouterLink>
              ))}
            </div>
          </section>
        ))
      )}
    </>
  );
}
