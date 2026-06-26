-- =============================================
-- Datos de prueba para Rate My Prof
-- Ejecutar en PostgreSQL después de iniciar la app
-- (la app crea las tablas automáticamente)
-- =============================================

-- Materias
INSERT INTO materias (nombre) VALUES
('Fonética Inglesa I'),
('Gramática Inglesa I'),
('Literatura Inglesa'),
('Práctica de la Pronunciación'),
('Lengua Francesa I');

-- Profesores
INSERT INTO profesores (nombre, apellido) VALUES
('María', 'González'),
('Carlos', 'Fernández'),
('Ana', 'Martínez'),
('Laura', 'Rodríguez'),
('Pablo', 'López');

-- Cátedras (profesor + materia)
INSERT INTO catedras (profesor_id, materia_id) VALUES
(1, 1),  -- González da Fonética Inglesa I
(2, 1),  -- Fernández da Fonética Inglesa I
(3, 2),  -- Martínez da Gramática Inglesa I
(4, 2),  -- Rodríguez da Gramática Inglesa I
(1, 3),  -- González da Literatura Inglesa
(5, 4),  -- López da Práctica de la Pronunciación
(3, 5);  -- Martínez da Lengua Francesa I

-- Usuario de prueba (password: "password123" hasheado con BCrypt)
INSERT INTO usuarios (nombre, email, password, fecha_registro) VALUES
('Estudiante Test', 'test@test.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 NOW());
