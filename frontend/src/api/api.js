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

/** GET /api/catedras/{id}/reviews?orden=fecha|utiles → [ReviewView] */
export function getReviewsDeCatedra(catedraId, orden = 'fecha') {
  return get(`/api/catedras/${catedraId}/reviews?orden=${orden}`);
}

/** GET /api/buscar?q=... → ResultadosBusqueda { consulta, materias, catedras } */
export function buscar(q) {
  return get(`/api/buscar?q=${encodeURIComponent(q)}`);
}
