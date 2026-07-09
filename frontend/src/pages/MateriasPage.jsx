import { Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { getMaterias } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';

/**
 * Pantalla inicial: la lista de materias. Cada una linkea a sus cátedras.
 * Es la más simple del plan; sirve para fijar el patrón
 * "useApi → mostrar cargando/error/datos" que repiten todas las demás.
 */
export default function MateriasPage() {
  const { data: materias, cargando, error } = useApi(getMaterias, []);

  if (cargando) return <Cargando />;
  if (error) return <ErrorMensaje error={error} />;

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Materias
      </Typography>

      {materias.length === 0 ? (
        <Vacio>No hay materias cargadas.</Vacio>
      ) : (
        <Paper>
          <List disablePadding>
            {materias.map((m) => (
              <ListItemButton
                key={m.id}
                component={RouterLink}
                to={`/materias/${m.id}`}
                divider
              >
                <ListItemText primary={m.nombre} />
                <ChevronRightIcon color="action" />
              </ListItemButton>
            ))}
          </List>
        </Paper>
      )}
    </>
  );
}
