// Opciones de "cuatrimestre cursado" para el form de review, de la más reciente
// a la más vieja ("2C 2026" ... "1C 2018"). Es el espejo de Cuatrimestre.java
// en el backend: el 2C del año actual se habilita desde julio, y no hay nada
// anterior a 2018. Si se cambia la regla acá hay que cambiarla allá también.

const PRIMER_ANIO = 2018;

export function opcionesCuatrimestre() {
  const hoy = new Date();
  const anioActual = hoy.getFullYear();
  // getMonth() es 0-based: 6 = julio.
  const ultimoCuatri = hoy.getMonth() >= 6 ? 2 : 1;

  const opciones = [];
  for (let anio = anioActual; anio >= PRIMER_ANIO; anio--) {
    for (let c = anio === anioActual ? ultimoCuatri : 2; c >= 1; c--) {
      opciones.push(`${c}C ${anio}`);
    }
  }
  return opciones;
}
