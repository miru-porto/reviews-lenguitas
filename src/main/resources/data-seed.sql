-- =====================================================================
-- Semilla del catálogo real del Profesorado de Inglés (versión SQL).
--
-- Es el espejo de DataSeeder.java (que siembra solo al arrancar contra
-- una base vacía): si se cambia uno hay que cambiar el otro. Esta
-- versión existe para servir de base a la migración de datos de Flyway.
--
-- Ejecutar SOLO sobre una base con las tablas creadas y vacías: si la
-- app ya arrancó contra la base vacía, el DataSeeder ya sembró esto
-- mismo y correrlo de nuevo duplicaría el catálogo.
--
-- Fuentes:
--  - Materias y año de cursada: "materias y sistema de correlativas.pdf"
--    (solo la carrera de Inglés).
--  - Cátedras: horarios TM y TV del 2° cuatrimestre 2026. Solo publican
--    apellidos (a lo sumo una inicial): el nombre queda vacío.
-- =====================================================================

-- ===== Materias (anio = año de cursada según el plan; 5to = plan de 5 años) =====
INSERT INTO materias (id, nombre, anio) VALUES
-- 1er año
(1,  'Lengua Inglesa 1', 1),
(2,  'Gramática Inglesa 1', 1),
(3,  'Fonología y Práctica de Laboratorio 1', 1),
(4,  'Taller 1', 1),
(5,  'Taller 2', 1),
(6,  'Pedagogía', 1),
(7,  'Didáctica General', 1),
(8,  'Psicología Educacional', 1),
(9,  'Taller de Nuevas Tecnologías', 1),
(10, 'Taller de Lectura, Escritura y Oralidad (TLEO)', 1),
-- 2do año
(11, 'Lengua Inglesa 2', 2),
(12, 'Gramática Inglesa 2', 2),
(13, 'Fonología y Práctica de Laboratorio 2', 2),
(14, 'Cultura de los Pueblos de Habla Inglesa 1', 2),
(15, 'Didáctica Específica 1', 2),
(16, 'Didáctica Específica 2', 2),
(17, 'Creatividad 1', 2),
(18, 'Taller 3', 2),
(19, 'Taller 4', 2),
(20, 'Sujetos de la Educación 1', 2),
(21, 'Sistema y Política Educativa', 2),
(22, 'Metodología de la Investigación', 2),
(23, 'Educación Sexual Integral (ESI)', 2),
-- 3er año
(24, 'Lengua Inglesa 3', 3),
(25, 'Lingüística', 3),
(26, 'Fonología y Práctica de Laboratorio 3', 3),
(27, 'Literatura en Lengua Inglesa 1', 3),
(28, 'Literatura en Lengua Inglesa 2', 3),
(29, 'Sujetos de la Educación 2', 3),
(30, 'Creatividad 2', 3),
(31, 'Taller 5 (de Inicial y Primaria)', 3),
(32, 'Residencia para el Nivel Inicial y Primario', 3),
(33, 'Seminario de Investigación Acción 1', 3),
(34, 'Informática para la Enseñanza', 3),
(35, 'Instituciones Educativas', 3),
(36, 'Saberes Lúdicos, Corporales y Motores', 3),
(37, 'Trabajo de Campo', 3),
-- 4to año
(38, 'Lengua Inglesa 4', 4),
(39, 'Cultura de los Pueblos de Habla Inglesa 2', 4),
(40, 'Literatura en Lengua Inglesa 3', 4),
(41, 'Análisis y Redacción de Textos', 4),
(42, 'Teatro', 4),
(43, 'Taller 6 (de Nivel Medio)', 4),
(44, 'Residencia para el Nivel Medio', 4),
(45, 'Seminario de Investigación Acción 2', 4),
(46, 'Nuevos Escenarios, Cultura, Tecnología y Subjetividad', 4),
(47, 'Trabajo / Profesionalización Docente', 4),
(48, 'Filosofía', 4),
(49, 'TIC Aplicadas', 4),
-- 5to año (solo plan de 5 años)
(50, 'Portugués Ab Initio 1', 5),
(51, 'Portugués Ab Initio 2', 5),
(52, 'Cultura de los Pueblos de Habla Inglesa 3', 5),
(53, 'Literatura en Lengua Inglesa 4', 5),
(54, 'Residencia para el Nivel Superior', 5),
(55, 'Taller de Música', 5);

-- ===== Profesores (nombre = inicial si el horario la publica, si no vacío) =====
INSERT INTO profesores (id, nombre, apellido) VALUES
(1,  '', 'De Domenico'),
(2,  '', 'Ragno'),
(3,  '', 'Crottogini'),
(4,  '', 'Costa'),
(5,  'F.', 'Costa'),
(6,  '', 'Mortoro'),
(7,  '', 'Rossell'),
(8,  'V.', 'Fernandez'),
(9,  '', 'Rodríguez'),
(10, '', 'Caligaris'),
(11, '', 'Pérez Ponsa'),
(12, '', 'Massolo'),
(13, '', 'Carteau'),
(14, '', 'Morelli'),
(15, '', 'Bessega'),
(16, '', 'Castino'),
(17, '', 'Jaschek'),
(18, '', 'Del Regno'),
(19, '', 'Bergel'),
(20, '', 'Spina'),
(21, '', 'Belser'),
(22, '', 'Eichenbronner'),
(23, '', 'Rosenfeld'),
(24, '', 'Olivera'),
(25, '', 'Berardi'),
(26, '', 'Querales'),
(27, '', 'Kandel'),
(28, '', 'Tordoni'),
(29, '', 'Derman'),
(30, '', 'Banfi'),
(31, '', 'Almagro'),
(32, '', 'Luccon'),
(33, '', 'Benítez'),
(34, '', 'Fernández Armendáriz'),
(35, '', 'Ferraro'),
(36, '', 'Roldán'),
(37, '', 'Chervonko'),
(38, '', 'Asereny'),
(39, '', 'Longobardi'),
(40, '', 'Accardo'),
(41, '', 'Durán'),
(42, '', 'Ferrari'),
(43, '', 'Veneroso'),
(44, '', 'Gentile'),
(45, '', 'Ambao'),
(46, '', 'Villarejo'),
(47, '', 'Rivas'),
(48, '', 'Veronelli'),
(49, '', 'Verdelli'),
(50, '', 'Prado'),
(51, '', 'Rivarola'),
(52, '', 'Muiño'),
(53, '', 'Dayan'),
(54, '', 'Esmoris'),
(55, '', 'Kirsanov'),
(56, '', 'Plencovich'),
(57, '', 'Ferreyra Fernández'),
(58, '', 'Jacovkis'),
(59, '', 'Curatolo'),
(60, '', 'Carrió'),
(61, '', 'Ertel'),
(62, '', 'Adem'),
(63, '', 'Clessi'),
(64, '', 'Tabakian'),
(65, '', 'Arriagada'),
(66, '', 'Rodrigues Da Silva'),
(67, '', 'Perduca'),
(68, '', 'De Carlos'),
(69, '', 'Raviolo');

-- ===== Cátedras del 2° cuatrimestre 2026 (profesor_id, materia_id) =====
-- Las materias sin cátedra no se dictan este cuatrimestre (residencias,
-- Cultura 2, Portugués Ab Initio 1).
INSERT INTO catedras (profesor_id, materia_id) VALUES
-- 1er año
(1, 1), (2, 1), (3, 1), (4, 1),                    -- Lengua Inglesa 1
(6, 2), (7, 2), (8, 2), (9, 2), (10, 2),           -- Gramática Inglesa 1
(11, 3), (12, 3), (13, 3), (14, 3),                -- Fonología y Práctica de Lab. 1
(15, 4), (16, 4),                                  -- Taller 1
(17, 5), (15, 5), (5, 5),                          -- Taller 2
(18, 6), (19, 6),                                  -- Pedagogía
(20, 7), (21, 7), (18, 7),                         -- Didáctica General
(22, 8), (23, 8),                                  -- Psicología Educacional
(24, 9), (25, 9),                                  -- Taller de Nuevas Tecnologías
(26, 10), (27, 10),                                -- TLEO
-- 2do año
(28, 11), (3, 11), (29, 11),                       -- Lengua Inglesa 2
(30, 12), (31, 12),                                -- Gramática Inglesa 2
(13, 13), (32, 13), (33, 13),                      -- Fonología y Práctica de Lab. 2
(34, 14), (35, 14), (36, 14),                      -- Cultura Inglesa 1
(37, 15), (38, 15),                                -- Didáctica Específica 1
(39, 16),                                          -- Didáctica Específica 2
(40, 17),                                          -- Creatividad 1
(15, 18), (16, 18),                                -- Taller 3
(16, 19), (5, 19),                                 -- Taller 4
(42, 20), (43, 20),                                -- Sujetos de la Educación 1
(44, 21), (18, 21), (45, 21),                      -- Sistema y Política Educativa
(46, 22), (47, 22),                                -- Metodología de la Investigación
(48, 23), (40, 23),                                -- ESI
-- 3er año
(10, 24), (39, 24),                                -- Lengua Inglesa 3
(10, 25), (49, 25),                                -- Lingüística
(12, 26), (32, 26), (50, 26),                      -- Fonología y Práctica de Lab. 3
(4, 27),                                           -- Literatura en Lengua Inglesa 1
(4, 28),                                           -- Literatura en Lengua Inglesa 2
(51, 29),                                          -- Sujetos de la Educación 2
(41, 30), (40, 30),                                -- Creatividad 2
(52, 31),                                          -- Taller 5
(30, 33),                                          -- Seminario de Investigación Acción 1
(25, 34), (53, 34),                                -- Informática para la Enseñanza
(54, 35), (55, 35),                                -- Instituciones Educativas
(56, 36),                                          -- Saberes Lúdicos
(18, 37), (22, 37), (54, 37), (44, 37), (21, 37),  -- Trabajo de Campo
(20, 37), (55, 37), (45, 37), (23, 37), (19, 37),
-- 4to año
(57, 38), (39, 38),                                -- Lengua Inglesa 4
(34, 40), (58, 40),                                -- Literatura en Lengua Inglesa 3
(59, 41), (60, 41),                                -- Análisis y Redacción de Textos
(61, 42),                                          -- Teatro
(16, 43),                                          -- Taller 6
(62, 45),                                          -- Seminario de Investigación Acción 2
(46, 46),                                          -- Nuevos Escenarios
(63, 47), (47, 47),                                -- Trabajo / Profesionalización Docente
(64, 48), (65, 48),                                -- Filosofía
(53, 49),                                          -- TIC Aplicadas
-- 5to año
(66, 51),                                          -- Portugués Ab Initio 2
(34, 52), (67, 52),                                -- Cultura Inglesa 3
(34, 53), (58, 53),                                -- Literatura en Lengua Inglesa 4
(68, 55), (69, 55);                                -- Taller de Música

-- ===== Usuarios: admin (DNI 99999999) y usuario de prueba (DNI 12345678) =====
INSERT INTO usuarios (nombre, dni, rol, fecha_registro) VALUES
('Admin', '99999999', 'ADMIN', NOW()),
('Estudiante Test', '12345678', 'USER', NOW());

-- Los INSERT usan ids explícitos: hay que avanzar las secuencias para que
-- los próximos INSERT de la app no choquen con ids ya usados.
SELECT setval('materias_id_seq', (SELECT MAX(id) FROM materias));
SELECT setval('profesores_id_seq', (SELECT MAX(id) FROM profesores));
SELECT setval('catedras_id_seq', (SELECT MAX(id) FROM catedras));
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));
