import { IconCoffee } from './icons';

const CAFECITO_URL = 'https://cafecito.app/miru-porto';

/**
 * Botón flotante de "Invitame un cafecito" (donaciones). Fijo abajo a la
 * derecha en todas las páginas. En desktop es una pill con texto; en mobile
 * se reduce a un círculo con solo el ícono (lo maneja el CSS .cafecito-fab).
 */
export default function Cafecito() {
  return (
    <a
      href={CAFECITO_URL}
      target="_blank"
      rel="noopener noreferrer"
      className="btn btn-primary cafecito-fab elev-lg"
      title="Invitame un cafecito"
      aria-label="Invitame un cafecito"
    >
      <IconCoffee size={20} />
      <span className="cafecito-texto">Invitame un cafecito</span>
    </a>
  );
}
