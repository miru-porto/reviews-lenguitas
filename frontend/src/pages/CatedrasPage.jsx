import { useParams, Link as RouterLink } from 'react-router-dom';
import { getCatedrasDeMateria } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import RatingEstrellas from '../components/RatingEstrellas';
import Button from '../components/ui/Button';
import Avatar from '../components/ui/Avatar';
import { IconArrowLeft } from '../components/ui/icons';

/**
 * Cátedras de una materia, cada una con su promedio de rating, en cards que
 * linkean a la página de reviews. El id de la materia sale de la URL con
 * useParams; se lo pasamos como dependencia a useApi para que recargue si el
 * usuario navega de una materia a otra.
 */
export default function CatedrasPage() {
  const { materiaId } = useParams();
  const {
    data: catedras,
    cargando,
    error,
  } = useApi(() => getCatedrasDeMateria(materiaId), [materiaId]);

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  const nombreMateria = catedras[0]?.nombreMateria ?? 'Materia';

  return (
    <>
      <Button as={RouterLink} to="/materias" variant="ghost" icon={IconArrowLeft} style={{ marginBottom: 'var(--space-4)' }}>
        Materias
      </Button>

      <h2 style={{ fontSize: 34 }}>{nombreMateria}</h2>
      <p className="text-muted" style={{ marginBottom: 'var(--space-6)' }}>
        {catedras.length} {catedras.length === 1 ? 'cátedra' : 'cátedras'} · elegí una para ver sus reviews.
      </p>

      {catedras.length === 0 ? (
        <Vacio>Esta materia todavía no tiene cátedras.</Vacio>
      ) : (
        <div className="prof-grid">
          {catedras.map((c) => {
            const nombreCompleto = `${c.nombreProfesor} ${c.apellidoProfesor}`.trim();
            const tieneReviews = c.cantidadReviews > 0;
            return (
              <RouterLink key={c.catedraId} to={`/catedras/${c.catedraId}`} className="card card-hover elev-sm" style={{ gap: 'var(--space-3)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
                  <Avatar nombre={c.apellidoProfesor} size={48} variant={tieneReviews ? 'accent' : 'neutral'} />
                  <span className="card-title" style={{ fontSize: 19 }}>{nombreCompleto}</span>
                </div>
                {tieneReviews ? (
                  <RatingEstrellas promedio={c.promedioRating} total={c.cantidadReviews} />
                ) : (
                  <span className="text-muted" style={{ fontSize: 12 }}>
                    Todavía nadie la reseñó · sé la primera
                  </span>
                )}
              </RouterLink>
            );
          })}
        </div>
      )}
    </>
  );
}
