import { createContext, useContext, useEffect, useState } from 'react';

/**
 * Tema claro/oscuro de Nocturne. El valor se guarda en `data-theme` del <html>
 * (los tokens CSS de styles.css cambian según ese atributo) y se persiste en
 * localStorage. Por defecto arranca oscuro, que es la identidad del diseño.
 */
const ThemeContext = createContext(null);

function temaInicial() {
  const guardado = localStorage.getItem('tema');
  return guardado === 'light' || guardado === 'dark' ? guardado : 'dark';
}

export function ThemeProvider({ children }) {
  const [tema, setTema] = useState(temaInicial);

  useEffect(() => {
    document.documentElement.dataset.theme = tema;
    localStorage.setItem('tema', tema);
  }, [tema]);

  const toggle = () => setTema((t) => (t === 'dark' ? 'light' : 'dark'));

  return (
    <ThemeContext.Provider value={{ tema, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
