-- =====================================================================
-- V3: cátedra de Sandra Vigna en Lengua Inglesa 1.
--
-- Faltaba en el catálogo del seed (V2), que se armó desde los horarios
-- publicados. A diferencia del resto de los profesores de V2, acá sí se
-- conoce el nombre de pila.
--
-- No se usan ids explícitos: las secuencias ya quedaron avanzadas en V2,
-- así que la identity asigna los próximos libres.
-- =====================================================================

INSERT INTO profesores (nombre, apellido) VALUES ('Sandra', 'Vigna');

INSERT INTO catedras (profesor_id, materia_id)
SELECT p.id, m.id
FROM profesores p, materias m
WHERE p.nombre = 'Sandra' AND p.apellido = 'Vigna'
  AND m.nombre = 'Lengua Inglesa 1';
