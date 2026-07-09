import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Typography from '@mui/material/Typography';

/** Spinner centrado, para mientras carga una pantalla. */
export function Cargando() {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
      <CircularProgress />
    </Box>
  );
}

/** Mensaje de error de la API. */
export function ErrorMensaje({ error }) {
  return (
    <Alert severity="error" sx={{ my: 2 }}>
      {error?.message || 'Ocurrió un error inesperado'}
    </Alert>
  );
}

/** Texto gris para listas vacías ("no hay nada todavía"). */
export function Vacio({ children }) {
  return (
    <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
      {children}
    </Typography>
  );
}
