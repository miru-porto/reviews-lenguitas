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
  // usuario: el UsuarioView { id, dni, nombre } o null si no hay sesión.
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
   * Ingreso por DNI. Devuelve el usuario si el DNI existe. Si no existe, la API
   * responde 404 y api.login lanza ApiError(status 404): lo dejamos propagar
   * para que la pantalla de login muestre el paso de registro.
   */
  async function login(dni) {
    const u = await api.login(dni);
    setUsuario(u);
    return u;
  }

  /** Alta (DNI + nombre) que además deja la sesión iniciada. */
  async function registro(dni, nombre) {
    const u = await api.registro(dni, nombre);
    setUsuario(u);
    return u;
  }

  /** Cierra la sesión en el backend y limpia el estado local. */
  async function logout() {
    await api.logout();
    setUsuario(null);
  }

  const valor = { usuario, cargando, login, registro, logout };
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

/** Hook para leer el estado de sesión desde cualquier componente. */
export function useAuth() {
  return useContext(AuthContext);
}
