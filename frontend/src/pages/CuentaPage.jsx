import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import { Field, Input } from '../components/ui/Field';
import Dialog from '../components/ui/Dialog';
import { Cargando } from '../components/Estado';

/**
 * Mi cuenta: cambiar el apodo y borrar la cuenta. Es donde la política de
 * privacidad manda a ejercer los derechos de rectificación y supresión, así que
 * las dos cosas tienen que poder hacerse solas, sin escribirle a nadie.
 */
export default function CuentaPage() {
  const { usuario, cargando } = useAuth();

  if (cargando) return <Cargando />;
  if (!usuario) return <Navigate to="/login" replace />;

  // El formulario va en un componente aparte que se monta recién con el usuario
  // ya cargado. Si estuviera todo acá, el useState del apodo correría en el
  // primer render — con getMe todavía en vuelo y usuario en null — y el campo
  // quedaría vacío para siempre: los inicializadores de useState corren una sola
  // vez y no se enteran de que después llegó el dato.
  return <FormularioCuenta usuario={usuario} />;
}

function FormularioCuenta({ usuario }) {
  const { elegirApodo, borrarCuenta } = useAuth();
  const navigate = useNavigate();

  const [apodo, setApodo] = useState(usuario.nombre ?? '');
  const [error, setError] = useState('');
  const [ok, setOk] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [confirmando, setConfirmando] = useState(false);

  async function guardarApodo(e) {
    e.preventDefault();
    setError('');
    setOk(false);
    setEnviando(true);
    try {
      await elegirApodo(apodo.trim());
      setOk(true);
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  async function confirmarBorrado() {
    setError('');
    setEnviando(true);
    try {
      await borrarCuenta();
      navigate('/');
    } catch (err) {
      setError(err.message);
      setConfirmando(false);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div style={{ maxWidth: 520, margin: '0 auto', padding: 'var(--space-6) 0', display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
      <h1 style={{ fontSize: 30, fontWeight: 400, margin: 0 }}>Mi cuenta</h1>

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={guardarApodo} className="card elev-sm" style={{ padding: 'var(--space-6)', gap: 'var(--space-4)' }}>
        <div>
          <h2 style={{ fontSize: 19, margin: 0, marginBottom: 'var(--space-1)' }}>Tu apodo</h2>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Es lo único que se ve junto a tus reseñas. Si lo cambiás, cambia en
            todas las que ya escribiste.
          </p>
        </div>
        <Field label="Apodo" htmlFor="apodo">
          <Input id="apodo" value={apodo} onChange={(e) => { setApodo(e.target.value); setOk(false); }} maxLength={40} />
        </Field>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-3)' }}>
          <Button variant="primary" type="submit" disabled={enviando || apodo.trim() === usuario.nombre}>
            Guardar
          </Button>
          {ok && <span className="text-muted" style={{ fontSize: 13 }}>Guardado ✓</span>}
        </div>
      </form>

      <div className="card elev-sm" style={{ padding: 'var(--space-6)', gap: 'var(--space-4)' }}>
        <div>
          <h2 style={{ fontSize: 19, margin: 0, marginBottom: 'var(--space-1)' }}>Borrar mi cuenta</h2>
          <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
            Se elimina tu cuenta, todas tus reseñas y todos tus votos. Es
            inmediato y no se puede deshacer.
          </p>
        </div>
        <Button variant="danger" onClick={() => setConfirmando(true)} className="btn-acorde">
          Borrar mi cuenta
        </Button>
      </div>

      {confirmando && (
        <Dialog onClose={enviando ? undefined : () => setConfirmando(false)} labelledBy="borrar-cuenta-title">
          <div className="dialog-title" id="borrar-cuenta-title">¿Borrar tu cuenta?</div>
          <p className="text-muted" style={{ margin: 0 }}>
            Vas a perder tu cuenta, tus reseñas y tus votos, para siempre. Si
            volvés a entrar con Google después, empezás de cero.
          </p>
          <div className="dialog-actions">
            <Button variant="secondary" onClick={() => setConfirmando(false)} disabled={enviando}>
              Cancelar
            </Button>
            <Button variant="danger" onClick={confirmarBorrado} disabled={enviando}>
              Sí, borrar todo
            </Button>
          </div>
        </Dialog>
      )}
    </div>
  );
}
