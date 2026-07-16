# Rate My Prof - Lenguas Vivas Sofía E. B. de Spangenberg

Sitio web de reviews de profesores del colegio Lenguas Vivas. Los visitantes
navegan materias → cátedras → reviews sin loguearse; los usuarios registrados
pueden dejar una review por cátedra, editarla o borrarla.

Arquitectura: **API REST (Spring Boot) + frontend React (SPA)**. El backend
expone `/api/**` en JSON y no renderiza vistas; el front las arma en el navegador.

## Stack

**Backend**
- Java 21 / Spring Boot 3.5.16 (Web, Data JPA, Security, Validation)
- PostgreSQL + Flyway (migraciones)
- Lombok
- Maven

**Frontend** (`frontend/`)
- React 19 + Vite
- Material UI (MUI)
- React Router
- Autenticación por sesión (cookie): la SPA llama a la API con `credentials: 'include'`.

## Setup rápido

### 1. Crear la base de datos

```sql
CREATE DATABASE rate_my_prof;
```

El esquema lo crea Flyway automáticamente al arrancar (migraciones en
`src/main/resources/db/migration/`; Hibernate solo valida con `ddl-auto=validate`).

### 2. Configurar credenciales

La app lee la conexión desde variables de entorno, con valores por
defecto para correr en local (ver `src/main/resources/application.properties`).
Para usar tus propias credenciales, definí estas variables:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/rate_my_prof
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu_password_aqui
```

### 3. Ejecutar el backend (API)

```bash
mvn spring-boot:run
```

La API arranca en `http://localhost:8080` (endpoints bajo `/api`).

### 3b. Ejecutar el frontend (React)

En otra terminal:

```bash
cd frontend
npm install   # la primera vez
npm run dev
```

El front arranca en `http://localhost:5173` y consume la API del `8080`
(CORS ya está configurado para ese origen en desarrollo).

### 4. Datos de prueba

No hace falta cargar nada a mano: la migración `V2__seed.sql` carga el
catálogo real (materias, profesores y cátedras 2026) y dos usuarios: el
admin (DNI `99999999`) y un usuario de prueba (DNI `12345678`). Flyway
garantiza que corre una sola vez por base.

## Estructura del proyecto

```
src/main/java/com/lenguas/ratemyprof/
├── model/           # Entidades JPA (Usuario, Materia, Profesor, Catedra, Review)
├── repository/      # Interfaces JPA con queries custom
├── service/         # Lógica de negocio (filtros, ratings, autorización de reviews)
├── controller/api/  # REST controllers (JSON); no hay vistas server-side
├── dto/             # Entrada con validación (ReviewForm) y view models de salida
│                    # (ReviewView, CatedraView, RatingBreakdown)
├── exception/       # Excepciones de dominio + @RestControllerAdvice a JSON
└── config/          # Spring Security (filter chain de /api, CORS, CSRF)

src/main/resources/db/migration/   # Migraciones Flyway (V1 esquema, V2 seed)
frontend/                          # SPA React (Vite): pantallas, api/, auth/
```

Backend en capas: `REST Controller → Service → Repository → PostgreSQL`. Los
controllers solo traducen HTTP; la lógica (ratings, autorización, anti-duplicado)
vive en los services y se expone en JSON. Nada de vistas server-side: eso es React.

## Endpoints principales

Lectura pública (sin login):

| Método | Ruta | Qué hace |
|---|---|---|
| GET | `/api/materias` | Lista de materias (ordenadas por año y nombre) |
| GET | `/api/materias/{id}/catedras` | Cátedras de una materia, ordenadas por rating |
| GET | `/api/catedras/{id}` | Detalle de una cátedra + desglose de estrellas |
| GET | `/api/catedras/{id}/reviews` | Reviews paginadas (`?orden=fecha\|utiles`, `?page=&size=`) |
| GET | `/api/buscar?q=...` | Búsqueda por nombre de materia o profesor |

Sesión (login por DNI):

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/auth/login` · `/api/auth/registro` · `/api/auth/logout` | Autenticación |
| GET | `/api/auth/me` | Usuario actual (401 si no hay sesión) |
| POST/PUT/DELETE | `/api/reviews` · `/api/reviews/{id}` | Crear / editar / borrar review propia |
| POST | `/api/reviews/{id}/util` | Marcar/desmarcar una review como útil |

Solo ADMIN (rol en la sesión): escrituras `POST/PUT/DELETE` sobre
`/api/materias`, `/api/profesores` y `/api/catedras`.

La autorización de editar/borrar se verifica **en el servidor** (en
`ReviewService`), no solo ocultando botones en el front.

## Flujo principal

1. Usuario entra → ve lista de **materias**
2. Elige una materia → ve las **cátedras** ordenadas por rating
3. Entra a una cátedra → ve las **reviews** y el **desglose de estrellas**
4. Se registra/loguea → puede **dejar su review** (1-5 estrellas + comentario)
   y editarla o borrarla después
5. También puede marcar reviews de otros como **útiles**; la lista se puede
   ordenar por más recientes o por más útiles