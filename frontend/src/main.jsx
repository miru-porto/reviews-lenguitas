import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import theme from './theme';
import App from './App.jsx';
import { AuthProvider } from './auth/AuthContext';

// Punto de entrada. Envolvemos la app en:
//  - BrowserRouter: habilita el ruteo por URL (react-router).
//  - ThemeProvider + CssBaseline: aplican el tema de MUI y un reset de estilos.
//  - AuthProvider: estado de sesión disponible en toda la app.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>
);
