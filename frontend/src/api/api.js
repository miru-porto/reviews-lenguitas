// Módulo central de acceso a la API REST de Spring Boot.
//
// Todas las llamadas pasan por acá para no repetir la URL base ni la lógica de
// errores en cada pantalla. La API corre en otro origen (localhost:8080) que el
// front (localhost:5173), por eso `credentials: 'include'`: le dice al navegador
// que mande la cookie de sesión. El backend lo permite porque su CORS tiene
// allowCredentials=true para este origen.

const BASE_URL = 'http://localhost:8080';

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
    respuesta = await fetch(`${BASE_URL}${path}`, {
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
    respuesta = await fetch(`${BASE_URL}${path}`, {
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

/** GET /api/materias → [{ id, nombre }] */
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

// ---- Autenticación (Fase 4) ----

/**
 * POST /api/auth/login → UsuarioView si el DNI existe. Si no existe, el backend
 * responde 404: enviar lanza ApiError con status 404, y la pantalla de login lo
 * interpreta como "hay que registrarse".
 */
export function login(dni) {
  return enviar('POST', '/api/auth/login', { dni });
}

/** POST /api/auth/registro → 201 UsuarioView; deja la sesión iniciada. */
export function registro(dni, nombre) {
  return enviar('POST', '/api/auth/registro', { dni, nombre });
}

/** POST /api/auth/logout → 204 (sin cuerpo). */
export function logout() {
  return enviar('POST', '/api/auth/logout');
}

/** GET /api/auth/me → UsuarioView del usuario logueado, o 401 si no hay sesión. */
export function getMe() {
  return get('/api/auth/me');
}

// ---- Escritura de reviews (Fase 4c) ----

/**
 * POST /api/reviews → 201 { id, catedraId }. Requiere sesión (401 si no hay).
 * 409 si el usuario ya dejó una review para esa cátedra; 400 si el body es inválido.
 */
export function crearReview(catedraId, puntuacion, comentario) {
  return enviar('POST', '/api/reviews', { catedraId, puntuacion, comentario });
}

/**
 * PUT /api/reviews/{id} → 204. Edita una review propia; el backend verifica el
 * dueño (403 si es ajena, 404 si no existe, 400 si el body es inválido).
 */
export function editarReview(id, puntuacion, comentario) {
  return enviar('PUT', `/api/reviews/${id}`, { puntuacion, comentario });
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
