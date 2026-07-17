import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ScrollAlTope from './components/ScrollAlTope';
import MateriasPage from './pages/MateriasPage';
import CatedrasPage from './pages/CatedrasPage';
import ReviewsPage from './pages/ReviewsPage';
import BuscarPage from './pages/BuscarPage';
import LoginPage from './pages/LoginPage';
import ApodoPage from './pages/ApodoPage';
import CuentaPage from './pages/CuentaPage';
import PrivacidadPage from './pages/PrivacidadPage';
import AdminPage from './pages/AdminPage';
import { useAuth } from './auth/AuthContext';

/**
 * Mapa de rutas de la app. Todas cuelgan de <Layout /> (barra + buscador), así
 * que ese marco se pinta una sola vez y solo cambia el contenido según la URL.
 *
 *  /                     → lista de materias (redirige a /materias)
 *  /materias             → lista de materias
 *  /materias/:id         → cátedras de esa materia
 *  /catedras/:id         → reviews de esa cátedra
 *  /buscar?q=...         → resultados de búsqueda
 *  /login                → botón de ingreso con Google
 *  /apodo                → elección del apodo, tras el primer ingreso
 *  /cuenta               → cambiar el apodo y borrar la cuenta
 *  /privacidad           → política de privacidad
 *  /admin                → CRUD del catálogo (solo rol ADMIN; la página redirige si no)
 */
export default function App() {
  const { usuario, cargando } = useAuth();

  // Quien entró con Google pero todavía no eligió apodo no puede hacer nada
  // más: sus reseñas saldrían sin firma. Se lo empuja a /apodo desde donde sea.
  const faltaApodo = !cargando && usuario && !usuario.nombre;

  return (
    <>
      <ScrollAlTope />
      <Routes>
        <Route element={<Layout />}>
          <Route path="apodo" element={<ApodoPage />} />
          <Route path="privacidad" element={<PrivacidadPage />} />
          {faltaApodo ? (
            <Route path="*" element={<Navigate to="/apodo" replace />} />
          ) : (
            <>
              <Route index element={<MateriasPage />} />
              <Route path="materias" element={<MateriasPage />} />
              <Route path="materias/:materiaId" element={<CatedrasPage />} />
              <Route path="catedras/:catedraId" element={<ReviewsPage />} />
              <Route path="buscar" element={<BuscarPage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="cuenta" element={<CuentaPage />} />
              <Route path="admin" element={<AdminPage />} />
              {/* Cualquier ruta desconocida vuelve al inicio. */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </>
          )}
        </Route>
      </Routes>
    </>
  );
}
