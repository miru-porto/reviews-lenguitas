import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import Button from '../components/ui/Button';
import { IconGoogle } from '../components/ui/icons';

/**
 * Ingreso con Google, único camino de entrada. No hay formulario ni contraseña:
 * el botón navega a /api/oauth2/authorization/google, Spring Security manda a
 * Google, y Google devuelve a la app con la sesión iniciada (ver
 * AuthContext.login). Si Google falla, vuelve acá con ?error=google (ver
 * SecurityConfig.failureUrl).
 *
 * La app a propósito no guarda ninguna credencial ni identificador civil: no
 * los necesita para su trabajo, y lo que no se guarda no se filtra.
 */
export default function LoginPage() {
  const { login } = useAuth();
  const [params] = useSearchParams();
  const fallo = params.get('error') === 'google';

  return (
    <div
      className="card elev-sm"
      style={{ maxWidth: 420, margin: 'var(--space-8) auto', padding: 'var(--space-8)', gap: 'var(--space-4)', textAlign: 'center' }}
    >
      <div>
        <h3 style={{ marginBottom: 'var(--space-1)' }}>Ingresá</h3>
        <p className="text-muted" style={{ fontSize: 13, margin: 0 }}>
          Para dejar reviews, votar las útiles y editar las tuyas.
        </p>
      </div>

      {fallo && (
        <div className="alert alert-error">
          No pudimos completar el ingreso con Google. Probá de nuevo.
        </div>
      )}

      <Button variant="primary" icon={IconGoogle} onClick={login} block>
        Ingresar con Google
      </Button>

      <p className="text-muted" style={{ fontSize: 12, margin: 0 }}>
        Tus reseñas se publican con un apodo que elegís vos, nunca con tu nombre
        de Google.
      </p>
    </div>
  );
}
