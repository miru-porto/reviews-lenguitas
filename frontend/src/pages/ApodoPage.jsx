import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import { Field, Input } from '../components/ui/Field';
import { Cargando } from '../components/Estado';

/**
 * Elección del apodo, después del primer ingreso con Google.
 *
 * Es un paso aparte y no algo que se tome de Google a propósito: Google
 * devuelve el nombre real, y publicar críticas a profesores firmadas con el
 * nombre y apellido de cada estudiante es exactamente lo que esta app no debe
 * hacer. Google dice quién sos ante el sistema; el apodo es lo que ve el resto.
 */
export default function ApodoPage() {
  const { usuario, cargando, elegirApodo } = useAuth();
  const navigate = useNavigate();

  const [apodo, setApodo] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (cargando) return <Cargando />;
  // Sin sesión no hay apodo que elegir; y si ya tiene, no hay nada que hacer acá.
  if (!usuario) return <Navigate to="/login" replace />;
  if (usuario.nombre) return <Navigate to="/" replace />;

  async function guardar(e) {
    e.preventDefault();
    setError('');
    if (apodo.trim().length < 2) {
      setError('El apodo tiene que tener al menos 2 caracteres.');
      return;
    }
    setEnviando(true);
    try {
      await elegirApodo(apodo.trim());
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="card elev-sm" style={{ maxWidth: 420, margin: 'var(--space-8) auto', padding: 'var(--space-8)' }}>
      <form onSubmit={guardar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
        <div>
          <h3 style={{ marginBottom: 'var(--space-1)' }}>Elegí tu apodo</h3>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Es lo único que se ve junto a tus reseñas. No usamos tu nombre de
            Google: eso queda entre vos y nosotras.
          </p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <Field label="Apodo" htmlFor="apodo">
          <Input
            id="apodo"
            value={apodo}
            onChange={(e) => setApodo(e.target.value)}
            autoFocus
            maxLength={40}
            placeholder="Ej: EstudianteDeIngles"
          />
        </Field>
        <Button variant="primary" type="submit" block disabled={enviando}>
          Listo, entrar
        </Button>
      </form>
    </div>
  );
}
