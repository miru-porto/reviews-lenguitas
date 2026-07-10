import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Alert from '@mui/material/Alert';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useAuth } from '../auth/AuthContext';

/**
 * Ingreso por DNI, en dos pasos:
 *  1. "dni": se pide el DNI. Si ya está registrado, entra directo.
 *  2. "registro": si el DNI no existía (la API respondió 404), se muestra este
 *     paso para elegir un nombre (nick) y crear la cuenta. Tiene "volver" por si
 *     el DNI estaba mal tipeado.
 */
export default function LoginPage() {
  const { login, registro } = useAuth();
  const navigate = useNavigate();

  const [paso, setPaso] = useState('dni'); // 'dni' | 'registro'
  const [dni, setDni] = useState('');
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function ingresar(e) {
    e.preventDefault();
    setError('');

    // Validación local para dar un mensaje preciso sin ida y vuelta al server.
    if (!/^\d{7,8}$/.test(dni.trim())) {
      setError('El DNI debe tener 7 u 8 dígitos.');
      return;
    }

    setEnviando(true);
    try {
      await login(dni.trim());
      navigate('/');
    } catch (err) {
      // 404 = DNI no registrado → pasamos al alta con el mismo DNI.
      if (err.status === 404) {
        setPaso('registro');
      } else {
        setError(err.message);
      }
    } finally {
      setEnviando(false);
    }
  }

  async function crearCuenta(e) {
    e.preventDefault();
    setError('');

    if (nombre.trim() === '') {
      setError('Ingresá un nombre.');
      return;
    }

    setEnviando(true);
    try {
      await registro(dni.trim(), nombre.trim());
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  function volver() {
    setPaso('dni');
    setNombre('');
    setError('');
  }

  return (
    <Paper sx={{ maxWidth: 420, mx: 'auto', mt: 4, p: 4 }}>
      {paso === 'dni' ? (
        <Box component="form" onSubmit={ingresar}>
          <Typography variant="h5" gutterBottom>
            Ingresar
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            Ingresá tu DNI para entrar o crear tu cuenta.
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <TextField
            label="DNI"
            value={dni}
            onChange={(e) => setDni(e.target.value)}
            autoFocus
            fullWidth
            slotProps={{ htmlInput: { inputMode: 'numeric', maxLength: 8 } }}
            sx={{ mb: 3 }}
          />
          <Button type="submit" variant="contained" fullWidth disabled={enviando}>
            Continuar
          </Button>
        </Box>
      ) : (
        <Box component="form" onSubmit={crearCuenta}>
          <Button
            onClick={volver}
            startIcon={<ArrowBackIcon />}
            size="small"
            sx={{ mb: 1 }}
            disabled={enviando}
          >
            Volver
          </Button>

          <Typography variant="h5" gutterBottom>
            Primera vez por acá
          </Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            El DNI {dni} no está registrado. Elegí un nombre para aparecer en tus
            reseñas.
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <TextField
            label="Nombre"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            autoFocus
            fullWidth
            sx={{ mb: 3 }}
          />
          <Button type="submit" variant="contained" fullWidth disabled={enviando}>
            Crear cuenta y entrar
          </Button>
        </Box>
      )}
    </Paper>
  );
}
