import { useState, useEffect, useCallback } from 'react';

/**
 * Hook para cargar datos de la API. Encapsula el patrón que se repite en toda
 * pantalla: pedir datos al montar, mostrar "cargando", y quedarse con el
 * resultado o el error.
 *
 * @param {Function} fetchFn  función sin argumentos que devuelve una Promise
 *                            (ej: () => getMaterias()).
 * @param {Array}    deps     dependencias: cuando cambian, se vuelve a pedir.
 *                            Igual que el segundo argumento de useEffect.
 *
 * Devuelve { data, cargando, error, recargar }. `recargar` vuelve a pedir los
 * datos (útil tras crear/editar/borrar algo: refresca la lista sin remontar).
 */
export function useApi(fetchFn, deps = []) {
  const [data, setData] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  // Cada bump de `tick` reejecuta el efecto: es la palanca de recargar().
  const [tick, setTick] = useState(0);
  const recargar = useCallback(() => setTick((t) => t + 1), []);

  useEffect(() => {
    // `activo` evita actualizar el estado si el componente se desmontó (o las
    // deps cambiaron) antes de que la respuesta llegue: así no pisamos datos
    // nuevos con una respuesta vieja ni tocamos un componente ya desmontado.
    let activo = true;
    setCargando(true);
    setError(null);

    fetchFn()
      .then((resultado) => {
        if (activo) {
          setData(resultado);
        }
      })
      .catch((err) => {
        if (activo) {
          setError(err);
        }
      })
      .finally(() => {
        if (activo) {
          setCargando(false);
        }
      });

    return () => {
      activo = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, tick]);

  return { data, cargando, error, recargar };
}
