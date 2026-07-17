import { Link as RouterLink } from 'react-router-dom';

/**
 * Política de privacidad. Es un compromiso público, así que TIENE que decir la
 * verdad sobre lo que hace el sistema: si el código cambia lo que guarda, con
 * quién lo comparte o cuánto lo conserva, este texto cambia en el mismo commit.
 *
 * La URL (/privacidad) además está declarada en la pantalla de consentimiento de
 * Google Cloud, así que no se renombra sin actualizarla allá.
 */
export default function PrivacidadPage() {
  return (
    <article className="prosa" style={{ maxWidth: 680, margin: '0 auto', padding: 'var(--space-6) 0' }}>
      <h1 style={{ fontSize: 34, fontWeight: 400, marginBottom: 'var(--space-2)' }}>
        Política de privacidad
      </h1>
      <p className="text-muted" style={{ fontSize: 13, marginBottom: 'var(--space-6)' }}>
        Última actualización: 17 de julio de 2026
      </p>

      <p>
        Rate My Prof LV es un proyecto independiente hecho por una estudiante del
        Profesorado de Inglés del Lenguas Vivas. No está afiliado a la institución.
        Acá te contamos, sin vueltas, qué datos tuyos guardamos y qué hacemos con
        ellos.
      </p>

      <h2>Quién es responsable</h2>
      <p>
        Miranda Portocarrero. Para cualquier cosa relacionada con tus datos:{' '}
        <a href="mailto:portocarreromiranda@gmail.com">portocarreromiranda@gmail.com</a>.
      </p>

      <h2>Qué guardamos</h2>
      <p>Cuando entrás con Google, guardamos solamente:</p>
      <ul>
        <li>
          <strong>Tu identificador de Google</strong>: un número que Google usa
          para reconocerte. Nos sirve para saber que sos vos cuando volvés.
        </li>
        <li>
          <strong>Tu dirección de correo</strong>. No se muestra en ningún lado
          del sitio ni se le da a nadie. La usamos para reconocer a la
          administradora y para poder contactarte si hiciera falta.
        </li>
        <li>
          <strong>El apodo que elegís</strong> y la fecha en que te registraste.
        </li>
      </ul>
      <p>Y de tu actividad en el sitio:</p>
      <ul>
        <li>Tus reseñas: puntuación, comentario, cuatrimestre y fecha.</li>
        <li>Qué reseñas de otras personas marcaste como útiles.</li>
      </ul>

      <h2>Qué NO guardamos</h2>
      <p>
        Esto importa tanto como lo anterior. <strong>No pedimos ni almacenamos tu
        DNI</strong> ni ningún documento de identidad. <strong>No guardamos tu
        nombre real</strong>: aunque Google nos lo ofrece cuando entrás, lo
        descartamos — por eso te pedimos un apodo aparte. Tampoco guardamos tu
        foto de perfil ni contraseñas (nunca tenemos ninguna: de eso se encarga
        Google).
      </p>
      <p>
        <strong>No usamos ningún sistema de analítica, publicidad ni
        seguimiento.</strong> No hay Google Analytics, ni píxeles, ni terceros
        mirando lo que hacés acá.
      </p>

      <h2>Qué se ve públicamente</h2>
      <p>
        De todo lo anterior, lo único visible para cualquier persona que entre al
        sitio es <strong>tu apodo junto a tus reseñas</strong>, con su puntuación,
        comentario, cuatrimestre y fecha. Tu correo y tu identificador de Google
        no salen nunca del servidor.
      </p>
      <p>
        Tené presente algo que ninguna configuración puede resolver: si en el
        texto de una reseña contás algo que te identifica, eso queda público. El
        apodo te cubre; lo que escribís, no.
      </p>

      <h2>Cookies</h2>
      <p>
        Usamos dos, y las dos son imprescindibles para que el sitio funcione: una
        mantiene tu sesión abierta para que no tengas que entrar en cada página, y
        la otra protege los formularios de un tipo de ataque llamado CSRF.{' '}
        <strong>No hay cookies de publicidad ni de seguimiento</strong>, y por eso
        no vas a ver el clásico cartel de consentimiento.
      </p>

      <h2>Con quién se comparte</h2>
      <p>
        Con nadie, en el sentido de que no vendemos ni cedemos tus datos. Pero el
        sitio se apoya en tres servicios que necesariamente los procesan:
      </p>
      <ul>
        <li><strong>Google</strong> — verifica tu identidad cuando entrás.</li>
        <li><strong>Vercel</strong> — sirve las páginas del sitio.</li>
        <li><strong>Render</strong> — corre el servidor de la aplicación.</li>
        <li><strong>Neon</strong> — aloja la base de datos.</li>
      </ul>
      <p>
        Los tres últimos guardan la información en servidores de{' '}
        <strong>Estados Unidos</strong>, así que tus datos salen de Argentina.
      </p>

      <h2>Cuánto tiempo</h2>
      <p>
        Mientras tengas la cuenta. Si la borrás, se va todo en el momento (ver
        abajo). Este es un proyecto personal y sin fines comerciales: si algún día
        dejara de existir, la base de datos se elimina.
      </p>

      <h2>Tus derechos</h2>
      <p>
        La Ley 25.326 de Protección de los Datos Personales te da derecho a
        acceder a tus datos, corregirlos y pedir que se borren. En la práctica:
      </p>
      <ul>
        <li>
          <strong>Ver y corregir</strong>: tus reseñas las editás vos desde el
          sitio, y tu apodo lo cambiás cuando quieras desde{' '}
          <RouterLink to="/cuenta">Mi cuenta</RouterLink>.
        </li>
        <li>
          <strong>Borrar</strong>: en <RouterLink to="/cuenta">Mi cuenta</RouterLink>{' '}
          tenés un botón que elimina tu cuenta, tus reseñas y tus votos. Es
          inmediato, definitivo y no necesitás pedirle permiso a nadie.
        </li>
        <li>
          <strong>Cualquier otra cosa</strong>: escribinos al correo de arriba.
        </li>
      </ul>
      <p className="text-muted" style={{ fontSize: 13 }}>
        La autoridad de aplicación en Argentina es la Agencia de Acceso a la
        Información Pública, ante la que podés reclamar si considerás que no
        respetamos tus derechos.
      </p>

      <h2>Si algo cambia</h2>
      <p>
        Si esta política cambia, se actualiza la fecha de arriba. Si el cambio
        fuera importante —por ejemplo, si algún día empezáramos a usar publicidad
        o analítica— lo vas a ver avisado en el sitio, no escondido acá.
      </p>
    </article>
  );
}
