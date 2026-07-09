import { useState } from 'react';
import { Outlet, Link as RouterLink, useNavigate } from 'react-router-dom';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import InputBase from '@mui/material/InputBase';
import { alpha } from '@mui/material/styles';
import SearchIcon from '@mui/icons-material/Search';
import SchoolIcon from '@mui/icons-material/School';

/**
 * Marco común de todas las pantallas: barra superior con el título (link al
 * inicio) y un buscador. El contenido de cada ruta se pinta donde va <Outlet />
 * (react-router lo reemplaza según la URL).
 */
export default function Layout() {
  const navigate = useNavigate();
  const [texto, setTexto] = useState('');

  // Al enviar el buscador vamos a /buscar?q=... — la pantalla de búsqueda lee
  // ese parámetro de la URL y pide los resultados.
  function onSubmit(e) {
    e.preventDefault();
    const q = texto.trim();
    if (q) {
      navigate(`/buscar?q=${encodeURIComponent(q)}`);
    }
  }

  return (
    <Box sx={{ minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <SchoolIcon sx={{ mr: 1 }} />
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{ color: 'inherit', textDecoration: 'none', flexGrow: 1 }}
          >
            Rate My Prof · Lenguas Vivas
          </Typography>

          <Box
            component="form"
            onSubmit={onSubmit}
            sx={{
              display: 'flex',
              alignItems: 'center',
              borderRadius: 1,
              bgcolor: (t) => alpha(t.palette.common.white, 0.15),
              '&:hover': { bgcolor: (t) => alpha(t.palette.common.white, 0.25) },
              px: 1,
            }}
          >
            <SearchIcon fontSize="small" />
            <InputBase
              placeholder="Buscar materia o profesor…"
              value={texto}
              onChange={(e) => setTexto(e.target.value)}
              sx={{ color: 'inherit', ml: 1, width: { xs: 120, sm: 220 } }}
            />
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
