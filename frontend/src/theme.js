import { createTheme } from '@mui/material/styles';

// Tema base de MUI. Centralizar los colores acá (en vez de estilos sueltos)
// hace que toda la app comparta la misma identidad visual, la que en la versión
// Thymeleaf daba el CSS a mano.
const theme = createTheme({
  palette: {
    primary: {
      main: '#3f51b5',
    },
    secondary: {
      main: '#f50057',
    },
    background: {
      default: '#f5f6fa',
    },
  },
  typography: {
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
  },
});

export default theme;
