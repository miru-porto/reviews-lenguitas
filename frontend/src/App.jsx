import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import MateriasPage from './pages/MateriasPage';
import CatedrasPage from './pages/CatedrasPage';
import ReviewsPage from './pages/ReviewsPage';
import BuscarPage from './pages/BuscarPage';

/**
 * Mapa de rutas de la app. Todas cuelgan de <Layout /> (barra + buscador), así
 * que ese marco se pinta una sola vez y solo cambia el contenido según la URL.
 *
 *  /                     → lista de materias (redirige a /materias)
 *  /materias             → lista de materias
 *  /materias/:id         → cátedras de esa materia
 *  /catedras/:id         → reviews de esa cátedra
 *  /buscar?q=...         → resultados de búsqueda
 */
export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<MateriasPage />} />
        <Route path="materias" element={<MateriasPage />} />
        <Route path="materias/:materiaId" element={<CatedrasPage />} />
        <Route path="catedras/:catedraId" element={<ReviewsPage />} />
        <Route path="buscar" element={<BuscarPage />} />
        {/* Cualquier ruta desconocida vuelve al inicio. */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
