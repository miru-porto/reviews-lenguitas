import { useState } from 'react';
import { Outlet, Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useTheme } from '../theme/ThemeContext';
import Button from './ui/Button';
import Cafecito from './ui/Cafecito';
import { IconSearch, IconSun, IconMoon, IconMenu } from './ui/icons';

/**
 * Marco común de todas las pantallas: barra superior sticky con la marca (link
 * al inicio), buscador, toggle de tema y sesión. En mobile el nav se compacta:
 * el buscador pasa a una fila propia full-width y las acciones de cuenta se
 * esconden en un menú hamburguesa. El contenido de cada ruta va en <Outlet />.
 */
export default function Layout() {
  const navigate = useNavigate();
  const { usuario, cargando, logout } = useAuth();
  const { tema, toggle } = useTheme();
  const [texto, setTexto] = useState('');
  const [menuAbierto, setMenuAbierto] = useState(false);

  async function onLogout() {
    setMenuAbierto(false);
    await logout();
    navigate('/');
  }

  // Al enviar el buscador vamos a /buscar?q=... — la pantalla de búsqueda lee
  // ese parámetro de la URL y pide los resultados.
  function onSubmit(e) {
    e.preventDefault();
    const q = texto.trim();
    if (q) navigate(`/buscar?q=${encodeURIComponent(q)}`);
  }

  return (
    <div style={{ minHeight: '100vh' }}>
      <header className="nav">
        <RouterLink
          to="/"
          style={{ display: 'flex', alignItems: 'center', gap: 10, textDecoration: 'none', color: 'inherit', marginRight: 'auto' }}
        >
          <span className="brand-logo">LV</span>
          <span className="nav-brand">
            Rate My Prof<span style={{ color: 'var(--color-accent)' }}> LV</span>
            <span className="brand-sub" style={{ fontFamily: 'var(--font-body)', fontSize: 11, letterSpacing: '.04em', color: 'color-mix(in srgb,var(--color-text) 55%, transparent)' }}>
              Profesorado de Inglés · Lenguas Vivas
            </span>
          </span>
        </RouterLink>

        <form
          onSubmit={onSubmit}
          className="input nav-search"
          style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 10px' }}
        >
          <IconSearch size={16} />
          <input
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Buscar materia o profe…"
            aria-label="Buscar"
            style={{ border: 0, background: 'transparent', color: 'inherit', outline: 'none', width: '100%', fontSize: 14 }}
          />
        </form>

        <Button
          variant="secondary"
          className="btn-icon"
          onClick={toggle}
          aria-label="Cambiar tono claro/oscuro"
          title="Cambiar tono claro/oscuro"
        >
          {tema === 'dark' ? <IconSun /> : <IconMoon />}
        </Button>

        {/* Acciones de cuenta — visibles en desktop. Mientras carga (getMe
            inicial) no mostramos nada para no parpadear "Ingresar"/"Salir". */}
        {!cargando && (
          <div className="acciones-desktop" style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
            {usuario ? (
              <>
                {usuario.rol === 'ADMIN' && (
                  <Button as={RouterLink} to="/admin" variant="ghost">Admin</Button>
                )}
                <RouterLink to="/cuenta" className="text-muted" style={{ fontSize: 14, textDecoration: 'none', color: 'inherit' }}>
                  {usuario.nombre}
                </RouterLink>
                <Button variant="secondary" onClick={onLogout}>Salir</Button>
              </>
            ) : (
              <Button as={RouterLink} to="/login" variant="primary">Ingresar</Button>
            )}
          </div>
        )}

        {/* Hamburguesa — solo mobile. */}
        <Button
          variant="secondary"
          className="btn-icon menu-mobile-btn"
          onClick={() => setMenuAbierto((v) => !v)}
          aria-label="Menú"
          aria-expanded={menuAbierto}
        >
          <IconMenu />
        </Button>

        {/* Menú desplegable de cuenta (mobile). */}
        {menuAbierto && (
          <>
            <div className="menu-overlay" onClick={() => setMenuAbierto(false)} />
            <div className="menu-mobile elev-lg">
              {!cargando && usuario ? (
                <>
                  <div className="text-muted" style={{ fontSize: 12, padding: '2px 4px 6px' }}>
                    Hola, {usuario.nombre}
                  </div>
                  {usuario.rol === 'ADMIN' && (
                    <RouterLink to="/admin" className="menu-item" onClick={() => setMenuAbierto(false)}>Admin</RouterLink>
                  )}
                  <RouterLink to="/cuenta" className="menu-item" onClick={() => setMenuAbierto(false)}>Mi cuenta</RouterLink>
                  <button type="button" className="menu-item" onClick={onLogout}>Salir</button>
                </>
              ) : (
                <RouterLink to="/login" className="menu-item" onClick={() => setMenuAbierto(false)}>Ingresar</RouterLink>
              )}
            </div>
          </>
        )}
      </header>

      <main className="container">
        <Outlet />
      </main>

      <footer className="footer">
        <span>Rate My Prof LV · proyecto independiente, sin afiliación con la institución</span>
        <RouterLink to="/privacidad">Privacidad</RouterLink>
      </footer>

      <Cafecito />
    </div>
  );
}
