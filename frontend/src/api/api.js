// Módulo central de acceso a la API REST de Spring Boot.
//
// Todas las llamadas pasan por acá para no repetir la lógica de errores en cada
// pantalla. Los paths son relativos (/api/...) a propósito: el front siempre le
// pega a su PROPIO origen y nunca al dominio real del backend. Quien reenvía es
// el proxy de Vite en desarrollo (vite.config.js) y el rewrite de Vercel en
// producción (frontend/vercel.json).
//
// El motivo es la cookie de sesión. Si el navegador la viera venir de otro
// dominio (onrender.com sobre una página vercel.app) sería una cookie de
// terceros: Safari la bloquea de fábrica y el login se rompería. Con un solo
// origen es first-party y no hay nada que negociar — tampoco CORS.
//
// `credentials: 'include'` igual hace falta para que la cookie viaje.

/**
 * Error de API con el status HTTP y el mensaje que devolvió el backend.
 * El backend responde los errores como {"error": "..."} (ver ApiError.java),
 * así que intentamos leer ese campo para mostrar algo útil.
 */
export class ApiError extends Error {
  constructor(mensaje, status) {
    super(mensaje);
    this.name = 'ApiError';
    this.status = status;
  }
}

/**
 * Hace un GET al path dado y devuelve el JSON ya parseado.
 * Si la respuesta no es 2xx, lanza ApiError con el mensaje del backend.
 */
async function get(path) {
  let respuesta;
  try {
    respuesta = await fetch(path, {
      credentials: 'include',
    });
  } catch {
    // fetch solo rechaza por fallos de red (servidor caído, sin conexión).
    throw new ApiError('No se pudo conectar con el servidor', 0);
  }

  return manejarRespuesta(respuesta);
}

/**
 * Lee una cookie por nombre desde document.cookie. La usamos para el token CSRF:
 * el backend lo deja en la cookie XSRF-TOKEN (legible por JS) y espera que se lo
 * devolvamos en un header en cada request que modifica datos.
 */
function leerCookie(nombre) {
  const match = document.cookie.match('(^|;)\\s*' + nombre + '\\s*=\\s*([^;]+)');
  return match ? decodeURIComponent(match.pop()) : null;
}

/**
 * POST/PUT/DELETE con cuerpo JSON. A diferencia de get, manda el token CSRF en
 * el header X-XSRF-TOKEN (Spring lo exige para métodos que modifican). El token
 * sale de la cookie que el backend ya escribió en algún GET previo.
 */
async function enviar(metodo, path, cuerpo) {
  let respuesta;
  try {
    respuesta = await fetch(path, {
      method: metodo,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'X-XSRF-TOKEN': leerCookie('XSRF-TOKEN') || '',
      },
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ApiError('No se pudo conectar con el servidor', 0);
  }

  return manejarRespuesta(respuesta);
}

/**
 * Traduce una respuesta HTTP a datos o a un ApiError. Si el backend mandó un
 * cuerpo {"error": "..."} lo usamos como mensaje; 204 (sin cuerpo) devuelve null.
 */
async function manejarRespuesta(respuesta) {
  if (!respuesta.ok) {
    let mensaje = `Error ${respuesta.status}`;
    try {
      const cuerpo = await respuesta.json();
      if (cuerpo && cuerpo.error) {
        mensaje = cuerpo.error;
      }
    } catch {
      // El cuerpo no era JSON; nos quedamos con el mensaje genérico.
    }
    throw new ApiError(mensaje, respuesta.status);
  }

  // 204 No Content (ej: logout) no trae cuerpo que parsear.
  if (respuesta.status === 204) {
    return null;
  }
  return respuesta.json();
}

// ---- Endpoints de lectura (Fase 3) ----

/**
 * GET /api/materias → [{ id, nombre, anio, promedioRating, cantidadCatedras,
 * cantidadReviews }] ordenadas por año y nombre. Los agregados vienen en la
 * misma respuesta: la portada los pinta por materia y suma los totales de la
 * cabecera sin pedir nada más. El admin usa esta misma lista e ignora los
 * campos que no le sirven.
 */
export function getMaterias() {
  return get('/api/materias');
}

/** GET /api/materias/{id}/catedras → [CatedraConRating] */
export function getCatedrasDeMateria(materiaId) {
  return get(`/api/materias/${materiaId}/catedras`);
}

/** GET /api/catedras/{id} → CatedraDetalle { catedra, rating } */
export function getCatedra(catedraId) {
  return get(`/api/catedras/${catedraId}`);
}

/**
 * GET /api/catedras/{id}/reviews?orden=fecha|utiles&page=N → página de reviews.
 * La respuesta es la forma paginada de Spring:
 *   { content: [ReviewView], page: { size, number, totalElements, totalPages } }
 * `page` es 0-based (la primera página es la 0). El tamaño lo decide el backend
 * (5 por defecto); no lo mandamos para tener un solo lugar que lo defina.
 */
export function getReviewsDeCatedra(catedraId, orden = 'fecha', pagina = 0) {
  return get(`/api/catedras/${catedraId}/reviews?orden=${orden}&page=${pagina}`);
}

/** GET /api/buscar?q=... → ResultadosBusqueda { consulta, materias, catedras } */
export function buscar(q) {
  return get(`/api/buscar?q=${encodeURIComponent(q)}`);
}

// ---- Autenticación (login con Google) ----
//
// El ingreso NO pasa por acá: es una navegación del navegador a
// /api/oauth2/authorization/google, que Spring Security intercepta y manda a
// Google (ver AuthContext.login). Un fetch no serviría: el flujo OAuth necesita
// que la persona vea la pantalla de Google y vuelva. Acá quedan solo las
// llamadas normales sobre una sesión ya iniciada.

/**
 * PUT /api/auth/apodo → UsuarioView con el apodo nuevo. Requiere sesión.
 * 400 si el apodo está vacío o no mide entre 2 y 40 caracteres.
 */
export function elegirApodo(apodo) {
  return enviar('PUT', '/api/auth/apodo', { apodo });
}

/** POST /api/auth/logout → 204 (sin cuerpo). Lo maneja Spring Security. */
export function logout() {
  return enviar('POST', '/api/auth/logout');
}

/**
 * DELETE /api/auth/cuenta → 204. Borra la cuenta, las reviews y los votos, y
 * cierra la sesión del lado del backend. Definitivo: no hay papelera.
 */
export function borrarCuenta() {
  return enviar('DELETE', '/api/auth/cuenta');
}

/** GET /api/auth/me → UsuarioView del usuario logueado, o 401 si no hay sesión. */
export function getMe() {
  return get('/api/auth/me');
}

// ---- Escritura de reviews (Fase 4c) ----

/**
 * POST /api/reviews → 201 { id, catedraId }. Requiere sesión (401 si no hay).
 * 409 si el usuario ya dejó una review para esa cátedra; 400 si el body es
 * inválido (incluye cuatrimestre vacío o fuera del rango 1C 2018..hoy).
 */
export function crearReview(catedraId, puntuacion, comentario, cuatrimestre) {
  return enviar('POST', '/api/reviews', { catedraId, puntuacion, comentario, cuatrimestre });
}

/**
 * PUT /api/reviews/{id} → 204. Edita una review propia; el backend verifica el
 * dueño (403 si es ajena, 404 si no existe, 400 si el body es inválido).
 */
export function editarReview(id, puntuacion, comentario, cuatrimestre) {
  return enviar('PUT', `/api/reviews/${id}`, { puntuacion, comentario, cuatrimestre });
}

/** DELETE /api/reviews/{id} → 204. Borra una review propia (403 si es ajena). */
export function borrarReview(id) {
  return enviar('DELETE', `/api/reviews/${id}`);
}

/**
 * POST /api/reviews/{id}/util → 204. Marca/desmarca (toggle) una review como útil.
 * 403 si es la review propia. Tras esto conviene recargar la lista para ver el conteo.
 */
export function votarUtil(id) {
  return enviar('POST', `/api/reviews/${id}/util`);
}

// ---- Administración del catálogo (Fase 5 / 2.6) ----
//
// Las lecturas son públicas; las escrituras exigen rol ADMIN en el backend
// (401 sin sesión, 403 con sesión de usuario común). Los 409 traen un mensaje
// explicativo: nombre duplicado, cátedra repetida, o borrado bloqueado porque
// hay datos que dependen (cátedras de una materia, reviews de una cátedra).

/** GET /api/profesores → [{ id, nombre, apellido }] ordenados por apellido. */
export function getProfesores() {
  return get('/api/profesores');
}

/** GET /api/catedras → [CatedraView { catedraId, materiaId, materiaNombre, nombreProfesor, apellidoProfesor }] */
export function getCatedras() {
  return get('/api/catedras');
}

/** POST /api/materias → 201 { id, nombre, anio }. 409 si el nombre ya existe. */
export function crearMateria(nombre, anio) {
  return enviar('POST', '/api/materias', { nombre, anio });
}

/** PUT /api/materias/{id} → 204. 409 si el nombre nuevo choca con otra materia. */
export function editarMateria(id, nombre, anio) {
  return enviar('PUT', `/api/materias/${id}`, { nombre, anio });
}

/** DELETE /api/materias/{id} → 204. 409 si la materia tiene cátedras. */
export function borrarMateria(id) {
  return enviar('DELETE', `/api/materias/${id}`);
}

/** POST /api/profesores → 201 { id, nombre, apellido }. El nombre es opcional. */
export function crearProfesor(nombre, apellido) {
  return enviar('POST', '/api/profesores', { nombre, apellido });
}

/** PUT /api/profesores/{id} → 204. */
export function editarProfesor(id, nombre, apellido) {
  return enviar('PUT', `/api/profesores/${id}`, { nombre, apellido });
}

/** DELETE /api/profesores/{id} → 204. 409 si el profesor tiene cátedras. */
export function borrarProfesor(id) {
  return enviar('DELETE', `/api/profesores/${id}`);
}

/** POST /api/catedras → 201 CatedraView. 409 si el par profesor+materia ya existe. */
export function crearCatedra(profesorId, materiaId) {
  return enviar('POST', '/api/catedras', { profesorId, materiaId });
}

/** DELETE /api/catedras/{id} → 204. 409 si la cátedra tiene reviews. */
export function borrarCatedra(id) {
  return enviar('DELETE', `/api/catedras/${id}`);
}
