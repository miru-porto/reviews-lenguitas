import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import { Field, Input } from '../components/ui/Field';
import { IconArrowLeft } from '../components/ui/icons';

/**
 * Ingreso por DNI, en dos pasos:
 *  1. "dni": se pide el DNI. Si ya está registrado, entra directo.
 *  2. "registro": si el DNI no existía (la API respondió 404), se muestra este
 *     paso para elegir un nombre (nick) y crear la cuenta.
 */
export default function LoginPage() {
  const { login, registro } = useAuth();
  const navigate = useNavigate();

  const [paso, setPaso] = useState('dni'); // 'dni' | 'registro'
  const [dni, setDni] = useState('');
  const [nombre, setNombre] = useState('');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function ingresar(e) {
    e.preventDefault();
    setError('');
    if (!/^\d{7,8}$/.test(dni.trim())) {
      setError('El DNI debe tener 7 u 8 dígitos.');
      return;
    }
    setEnviando(true);
    try {
      await login(dni.trim());
      navigate('/');
    } catch (err) {
      // 404 = DNI no registrado → pasamos al alta con el mismo DNI.
      if (err.status === 404) setPaso('registro');
      else setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  async function crearCuenta(e) {
    e.preventDefault();
    setError('');
    if (nombre.trim() === '') {
      setError('Ingresá un nombre.');
      return;
    }
    setEnviando(true);
    try {
      await registro(dni.trim(), nombre.trim());
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  }

  function volver() {
    setPaso('dni');
    setNombre('');
    setError('');
  }

  return (
    <div className="card elev-sm" style={{ maxWidth: 420, margin: 'var(--space-8) auto', padding: 'var(--space-8)', gap: 'var(--space-4)' }}>
      {paso === 'dni' ? (
        <form onSubmit={ingresar} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <div>
            <h3 style={{ marginBottom: 'var(--space-1)' }}>Ingresá</h3>
            <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
              Con tu DNI podés dejar reviews, votar las útiles y editar las tuyas.
            </p>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <Field label="DNI" htmlFor="dni">
            <Input
              id="dni"
              value={dni}
              onChange={(e) => setDni(e.target.value)}
              autoFocus
              inputMode="numeric"
              maxLength={8}
              placeholder="Ej: 40123456"
            />
          </Field>
          <Button variant="primary" type="submit" block disabled={enviando}>Continuar</Button>
        </form>
      ) : (
        <form onSubmit={crearCuenta} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <Button variant="ghost" icon={IconArrowLeft} onClick={volver} disabled={enviando} style={{ alignSelf: 'flex-start' }}>
            Volver
          </Button>

          <div>
            <h3 style={{ marginBottom: 'var(--space-1)' }}>Primera vez por acá</h3>
            <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
              El DNI {dni} no está registrado. Elegí un nombre para aparecer en tus reseñas.
            </p>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <Field label="Nombre" htmlFor="nombre">
            <Input id="nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} autoFocus placeholder="Un nombre o apodo" />
          </Field>
          <Button variant="primary" type="submit" block disabled={enviando}>Crear cuenta y entrar</Button>
        </form>
      )}
    </div>
  );
}
