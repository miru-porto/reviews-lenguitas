# Rate My Prof - Lenguas Vivas Sofía E. B. de Spangenberg

Sitio web de reviews de profesores del colegio Lenguas Vivas. Los visitantes
navegan materias → cátedras → reviews sin loguearse; los usuarios registrados
pueden dejar una review por cátedra, editarla o borrarla.

## Stack

- Java 21 / Spring Boot 3.5.16 (Web, Data JPA, Security, Validation)
- Thymeleaf para las vistas (server-side rendering)
- PostgreSQL
- Lombok
- Maven

## Setup rápido

### 1. Crear la base de datos

```sql
CREATE DATABASE rate_my_prof;
```

El esquema lo genera Hibernate automáticamente al arrancar (`ddl-auto=update`).

### 2. Configurar credenciales

La app lee la conexión desde variables de entorno, con valores por
defecto para correr en local (ver `src/main/resources/application.properties`).
Para usar tus propias credenciales, definí estas variables:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/rate_my_prof
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu_password_aqui
```

### 3. Ejecutar la app

```bash
mvn spring-boot:run
```

La app arranca en `http://localhost:8080`

### 4. Datos de prueba

No hace falta cargar nada a mano: el `DataSeeder` (en `config/`) carga
materias, profesores y cátedras de ejemplo la primera vez que la app arranca
contra una base vacía. Es idempotente: si ya hay materias, no hace nada.

## Estructura del proyecto

```
src/main/java/com/lenguas/ratemyprof/
├── model/           # Entidades JPA (Usuario, Materia, Profesor, Catedra, Review)
├── repository/      # Interfaces JPA con queries custom
├── service/         # Lógica de negocio (filtros, ratings, autorización de reviews)
├── controller/      # Endpoints y vistas Thymeleaf
├── dto/             # Entrada con validación (ReviewForm) y view models de salida
│                    # (ReviewView, CatedraView, RatingBreakdown)
└── config/          # Spring Security + DataSeeder
```

Arquitectura MVC en capas: `Controller → Service → Repository → PostgreSQL`,
con Thymeleaf renderizando los templates de `resources/templates/`. Los
controllers no contienen lógica de negocio y las vistas consumen DTOs, no
entidades JPA.

## Rutas principales

| Ruta | Acceso | Qué hace |
|---|---|---|
| `/` | público | Redirige a `/materias` |
| `/materias` | público | Lista de materias |
| `/materias/{id}` | público | Cátedras de una materia, ordenadas por rating |
| `/catedra/{id}` | público | Reviews de una cátedra + desglose de estrellas (orden por fecha o por votos útiles con `?orden=utiles`) |
| `/buscar?q=...` | público | Búsqueda por nombre de materia o profesor |
| `/review/nueva/{catedraId}` | logueado | Crear review (una por usuario por cátedra) |
| `/review/{id}/editar` | logueado (solo el autor) | Editar review propia |
| `/review/{id}/util` | logueado (no el autor) | Marcar/desmarcar una review como útil |
| `/review/{id}/borrar` | logueado (solo el autor) | Borrar review propia |
| `/login`, `/registro`, `/logout` | público | Autenticación |

La autorización de editar/borrar se verifica **en el servidor** (en
`ReviewService`), no solo ocultando botones en la vista.

## Flujo principal

1. Usuario entra → ve lista de **materias**
2. Elige una materia → ve las **cátedras** ordenadas por rating
3. Entra a una cátedra → ve las **reviews** y el **desglose de estrellas**
4. Se registra/loguea → puede **dejar su review** (1-5 estrellas + comentario)
   y editarla o borrarla después
5. También puede marcar reviews de otros como **útiles**; la lista se puede
   ordenar por más recientes o por más útiles