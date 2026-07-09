import { useParams, Link as RouterLink } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Card from '@mui/material/Card';
import CardActionArea from '@mui/material/CardActionArea';
import CardContent from '@mui/material/CardContent';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { getCatedrasDeMateria } from '../api/api';
import { useApi } from '../hooks/useApi';
import { Cargando, ErrorMensaje, Vacio } from '../components/Estado';
import RatingEstrellas from '../components/RatingEstrellas';

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

  // El nombre de la materia viene dentro de cada cátedra; si no hay cátedras,
  // usamos un título genérico.
  const nombreMateria = catedras[0]?.nombreMateria ?? 'Materia';

  return (
    <>
      <Button
        component={RouterLink}
        to="/materias"
        startIcon={<ArrowBackIcon />}
        sx={{ mb: 2 }}
      >
        Materias
      </Button>

      <Typography variant="h4" gutterBottom>
        {nombreMateria}
      </Typography>

      {catedras.length === 0 ? (
        <Vacio>Esta materia todavía no tiene cátedras.</Vacio>
      ) : (
        <Stack spacing={2}>
          {catedras.map((c) => (
            <Card key={c.catedraId}>
              <CardActionArea
                component={RouterLink}
                to={`/catedras/${c.catedraId}`}
              >
                <CardContent>
                  <Typography variant="h6">
                    {c.nombreProfesor} {c.apellidoProfesor}
                  </Typography>
                  <RatingEstrellas
                    promedio={c.promedioRating}
                    total={c.cantidadReviews}
                  />
                </CardContent>
              </CardActionArea>
            </Card>
          ))}
        </Stack>
      )}
    </>
  );
}
