import { useSearchParams, Link as RouterLink } from 'react-router-dom';
import { buscar } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import Avatar from '../components/ui/Avatar';
import { IconChevronRight } from '../components/ui/icons';

/**
 * Resultados de búsqueda. La consulta viaja en la URL (?q=...), no en el estado
 * del componente: así el resultado es linkeable/compartible y el back del
 * navegador funciona. La API agrupa por tipo: materias (van a su página de
 * cátedras) y cátedras (coincidencias por profesor, van directo a sus reviews).
 */
export default function BuscarPage() {
  const [searchParams] = useSearchParams();
  const q = searchParams.get('q') ?? '';

  const { data, cargando, error } = useApi(() => buscar(q), [q]);

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  const sinResultados = data.materias.length === 0 && data.catedras.length === 0;

  return (
    <>
      <h2>Resultados para «{data.consulta}»</h2>

      {sinResultados ? (
        <Vacio>No se encontraron materias ni profesores.</Vacio>
      ) : (
        <>
          {data.materias.length > 0 && (
            <section style={{ marginBottom: 'var(--space-8)' }}>
              <h5 className="text-muted" style={{ marginBottom: 'var(--space-3)' }}>Materias</h5>
              <div className="section-grid">
                {data.materias.map((m) => (
                  <RouterLink key={m.id} to={`/materias/${m.id}`} className="card card-hover elev-sm">
                    <div className="row-between" style={{ flexWrap: 'nowrap' }}>
                      <span className="card-title">{m.nombre}</span>
                      <IconChevronRight size={18} />
                    </div>
                  </RouterLink>
                ))}
              </div>
            </section>
          )}

          {data.catedras.length > 0 && (
            <section>
              <h5 className="text-muted" style={{ marginBottom: 'var(--space-3)' }}>Profesores</h5>
              <div className="prof-grid">
                {data.catedras.map((c) => (
                  <RouterLink key={c.catedraId} to={`/catedras/${c.catedraId}`} className="card card-hover elev-sm">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                      <Avatar nombre={c.apellidoProfesor} size={44} variant="accent" />
                      <div>
                        <div className="card-title" style={{ fontSize: 18 }}>
                          {`${c.nombreProfesor} ${c.apellidoProfesor}`.trim()}
                        </div>
                        <div className="text-muted" style={{ fontSize: 12 }}>{c.materiaNombre}</div>
                      </div>
                    </div>
                  </RouterLink>
                ))}
              </div>
            </section>
          )}
        </>
      )}
    </>
  );
}
