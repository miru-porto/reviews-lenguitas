import { useEffect } from 'react';
import { useLocation, useNavigationType } from 'react-router-dom';

/**
 * Lleva el scroll al tope cuando se entra a otra pantalla.
 * Sin esto el scroll llega a 0 igual, PERO de casualidad
 */
export default function ScrollAlTope() {
  const { pathname } = useLocation();
  const tipo = useNavigationType();

  useEffect(() => {
    if (tipo === 'POP') return;
    window.scrollTo(0, 0);
  }, [pathname, tipo]);

  return null;
}
