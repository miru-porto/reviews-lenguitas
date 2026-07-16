import { Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Box from '@mui/material/Box';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { getMaterias } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';

const NOMBRE_ANIO = {
  1: '1er año',
  2: '2do año',
  3: '3er año',
  4: '4to año',
  5: '5to año (plan de 5 años)',
};

/**
 * Pantalla inicial: las materias agrupadas por año de cursada (1ro a 5to,
 * según el plan de estudios). La API ya las manda ordenadas por año y nombre;
 * acá solo se cortan en secciones. Las materias sin año (creadas por un admin
 * sin el dato) caen en una sección "Sin año asignado" al final.
 */
export default function MateriasPage() {
  const { data: materias, cargando, error } = useApi(getMaterias, []);

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

  return (
    <>
      <Typography variant="h4" gutterBottom>
        Materias
      </Typography>

      {materias.length === 0 ? (
        <Vacio>No hay materias cargadas.</Vacio>
      ) : (
        [...grupos.entries()].map(([anio, lista]) => (
          <Box key={anio} sx={{ mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              {NOMBRE_ANIO[anio] ?? 'Sin año asignado'}
            </Typography>
            <Paper>
              <List disablePadding>
                {lista.map((m) => (
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
          </Box>
        ))
      )}
    </>
  );
}
