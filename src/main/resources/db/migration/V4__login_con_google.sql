-- =====================================================================
-- V4: la identidad pasa del DNI a la cuenta de Google.
--
-- El login por DNI no autenticaba: sin contraseña, cualquiera que supiera
-- un DNI entraba como esa persona, y los DNI circulan en listas de finales
-- y de asistencia. Encima no verificaba nada (no hay padrón contra el cual
-- chequear que un DNI sea real ni que sea de alguien del profesorado), así
-- que costaba un dato sensible sin dar control de acceso. Se reemplaza por
-- "Ingresar con Google": el 'sub' de Google es la nueva identidad.
--
-- POR QUÉ SE BORRAN LOS USUARIOS: ninguna fila existente tiene google_sub,
-- y no hay forma de deducirlo (habría que preguntarle a cada persona). O
-- sea que después de esta migración NINGUNA cuenta vieja podría iniciar
-- sesión igual: quedarían como filas muertas sosteniendo reviews de nadie.
-- Se borran ellas y lo que cuelga. En producción esto no cuesta nada (solo
-- estaba el admin sembrado por V2, sin reviews); en la base local se lleva
-- los usuarios y reviews de prueba.
-- =====================================================================

-- Primero lo que depende de usuarios (las FK no borran en cascada aposta).
DELETE FROM votos_util;
DELETE FROM reviews;
DELETE FROM usuarios;

-- El DNI desaparece del modelo.
ALTER TABLE usuarios DROP COLUMN dni;

-- La identidad nueva. NOT NULL sin default: la tabla quedó vacía arriba, así
-- que no hay filas que completar.
ALTER TABLE usuarios ADD COLUMN google_sub VARCHAR(255) NOT NULL UNIQUE;

-- Google siempre manda el email con el scope 'email', y lo necesitamos para
-- reconocer a la admin (ver ADMIN_EMAIL), así que deja de ser opcional.
ALTER TABLE usuarios ALTER COLUMN email SET NOT NULL;

-- El apodo se elige DESPUÉS del primer login (no se toma el nombre real de
-- Google), así que hay un instante en que el usuario existe sin apodo.
ALTER TABLE usuarios ALTER COLUMN nombre DROP NOT NULL;

-- Ya no se siembra ningún admin: el rol ADMIN se otorga al entrar, cuando el
-- email de Google coincide con ADMIN_EMAIL (ver UsuarioService).
