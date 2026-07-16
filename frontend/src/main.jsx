import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './styles.css';
import App from './App.jsx';
import { AuthProvider } from './auth/AuthContext';
import { ThemeProvider } from './theme/ThemeContext';

// Punto de entrada. Envolvemos la app en:
//  - BrowserRouter: habilita el ruteo por URL (react-router).
//  - ThemeProvider: tema claro/oscuro de Nocturne (data-theme en <html>).
//  - AuthProvider: estado de sesión disponible en toda la app.
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>
);
