import Button from './Button';

/**
 * Paginador simple (reemplaza al Pagination de MUI). Trabaja en base 0 como la
 * API: `pagina` es la actual, `total` la cantidad de páginas, `onCambiar(n)`
 * navega. Muestra ‹ Anterior · «X de Y» · Siguiente ›. No se renderiza si hay
 * una sola página.
 */
export default function Pager({ pagina, total, onCambiar }) {
  if (total <= 1) return null;

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 'var(--space-4)',
        marginTop: 'var(--space-6)',
      }}
    >
      <Button
        variant="secondary"
        disabled={pagina === 0}
        onClick={() => onCambiar(pagina - 1)}
      >
        ‹ Anterior
      </Button>
      <span className="text-muted" style={{ fontSize: 13 }}>
        {pagina + 1} de {total}
      </span>
      <Button
        variant="secondary"
        disabled={pagina >= total - 1}
        onClick={() => onCambiar(pagina + 1)}
      >
        Siguiente ›
      </Button>
    </div>
  );
}
