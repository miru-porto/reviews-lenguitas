import { useSearchParams, Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import { buscar } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';

/**
 * Resultados de búsqueda. La consulta viaja en la URL (?q=...), no en el estado
 * del componente: así el resultado es linkeable/compartible y el back del
 * navegador funciona. useSearchParams lee ese parámetro; cuando cambia, useApi
 * vuelve a pedir.
 *
 * La API agrupa por tipo: materias (van a su página de cátedras) y cátedras
 * (coincidencias por profesor, van directo a sus reviews).
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
      <Typography variant="h4" gutterBottom>
        Resultados para «{data.consulta}»
      </Typography>

      {sinResultados ? (
        <Vacio>No se encontraron materias ni profesores.</Vacio>
      ) : (
        <>
          {data.materias.length > 0 && (
            <>
              <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
                Materias
              </Typography>
              <Paper>
                <List disablePadding>
                  {data.materias.map((m) => (
                    <ListItemButton
                      key={m.id}
                      component={RouterLink}
                      to={`/materias/${m.id}`}
                      divider
                    >
                      <ListItemText primary={m.nombre} />
                    </ListItemButton>
                  ))}
                </List>
              </Paper>
            </>
          )}

          {data.catedras.length > 0 && (
            <>
              <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
                Profesores
              </Typography>
              <Paper>
                <List disablePadding>
                  {data.catedras.map((c) => (
                    <ListItemButton
                      key={c.catedraId}
                      component={RouterLink}
                      to={`/catedras/${c.catedraId}`}
                      divider
                    >
                      <ListItemText
                        primary={`${c.nombreProfesor} ${c.apellidoProfesor}`}
                        secondary={c.materiaNombre}
                      />
                    </ListItemButton>
                  ))}
                </List>
              </Paper>
            </>
          )}
        </>
      )}
    </>
  );
}
