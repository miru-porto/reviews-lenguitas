import { createContext, useContext, useEffect, useState } from 'react';
import * as api from '../api/api';

/**
 * Estado global de sesión. Cualquier componente puede saber quién está logueado
 * (o si no hay nadie) y disparar login/registro/logout, sin pasar props a mano
 * por todo el árbol. React resuelve esto con un "context": el AuthProvider lo
 * ofrece una vez arriba de todo, y useAuth lo consume donde haga falta.
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // usuario: el UsuarioView { id, nombre, rol } o null si no hay sesión.
  // `nombre` en null significa "entró con Google pero todavía no eligió apodo".
  const [usuario, setUsuario] = useState(null);
  // cargando: true hasta que sabemos si había una sesión previa (getMe inicial).
  const [cargando, setCargando] = useState(true);

  // Al montar preguntamos "¿quién soy?": si la cookie de sesión sigue viva, el
  // backend devuelve el usuario; si no, responde 401 y quedamos deslogueados.
  useEffect(() => {
    api
      .getMe()
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCargando(false));
  }, []);

  /**
   * Arranca el login con Google. No es un fetch: es una navegación de verdad,
   * porque el flujo OAuth se lo lleva el navegador a google.com y vuelve. Por
   * eso no devuelve nada — al volver, el AuthProvider se monta de nuevo y el
   * getMe de arriba encuentra la sesión ya iniciada.
   */
  function login() {
    window.location.href = '/api/oauth2/authorization/google';
  }

  /** Elige el apodo público (primer ingreso o cambio posterior). */
  async function elegirApodo(apodo) {
    const u = await api.elegirApodo(apodo);
    setUsuario(u);
    return u;
  }

  /** Cierra la sesión en el backend y limpia el estado local. */
  async function logout() {
    await api.logout();
    setUsuario(null);
  }

  const valor = { usuario, cargando, login, elegirApodo, logout };
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

/** Hook para leer el estado de sesión desde cualquier componente. */
export function useAuth() {
  return useContext(AuthContext);
}
