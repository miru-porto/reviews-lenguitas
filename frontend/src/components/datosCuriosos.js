/**
 * Datos sobre la lengua inglesa, para acompañar la espera del cold start
 * (ver Estado.jsx).
 *
 * REGLA: solo entran datos comprobables. El folclore de "fun facts" del inglés
 * está lleno de mitos que suenan bien y son falsos — los cien nombres esquimales
 * para la nieve, el récord de la palabra más larga, la etimología inventada de
 * turno. Esto lo leen futuras profesoras de inglés: si un dato no se puede
 * verificar en un diccionario o comprobar leyéndolo, no va.
 *
 * Por eso están elegidos los que se verifican solos: contá las letras del
 * pangrama, leé el "ough" en voz alta, armá la oración de los búfalos. No hay
 * superlativos ("la palabra más larga", "el idioma con más...") porque son
 * justo los que envejecen mal y los que nadie chequea.
 *
 * Agregar los que quieras — la única condición es la de arriba.
 */
export const DATOS_CURIOSOS = [
  '«The quick brown fox jumps over the lazy dog» usa las 26 letras del alfabeto inglés. A eso se le dice pangrama.',
  'Las mismas cuatro letras, cinco sonidos distintos: though, through, cough, rough y bough.',
  '«Buffalo buffalo Buffalo buffalo buffalo buffalo Buffalo buffalo» es una oración gramaticalmente correcta. Ciudad, animal y verbo, todo a la vez.',
  'A «queue» le podés sacar las últimas cuatro letras y se sigue pronunciando igual.',
  'El inglés no tiene terminación de futuro. Donde el español conjuga «hablaré», el inglés necesita un modal: «will speak».',
  'En inglés «I» va siempre en mayúscula, incluso en el medio de la oración. Ningún otro pronombre tiene ese privilegio.',
  'Los verbos irregulares más usados (be, have, go) son irregulares justamente porque son los más usados: tanto uso los protegió de regularizarse.',
];

/** Uno al azar, distinto del actual (para que rotar siempre se note). */
export function otroDato(actual) {
  if (DATOS_CURIOSOS.length < 2) return DATOS_CURIOSOS[0];
  let siguiente;
  do {
    siguiente = DATOS_CURIOSOS[Math.floor(Math.random() * DATOS_CURIOSOS.length)];
  } while (siguiente === actual);
  return siguiente;
}
