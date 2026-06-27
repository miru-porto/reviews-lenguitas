# Rate My Prof - Lenguas Vivas Sofía E. B. de Spangenberg

Sitio web de reviews de profesores del colegio Lenguas Vivas.

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Setup rápido

### 1. Crear la base de datos

```sql
CREATE DATABASE rate_my_prof;
```

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

### 4. Cargar datos de prueba (opcional)

Ejecutar `src/main/resources/data-seed.sql` en PostgreSQL para tener
materias, profesores y cátedras de ejemplo.

## Estructura del proyecto

```
src/main/java/com/lenguas/ratemyprof/
├── model/           # Entidades JPA (Usuario, Materia, Profesor, Catedra, Review)
├── repository/      # Interfaces JPA con queries custom
├── service/         # Lógica de negocio (filtros, ratings, auth)
├── controller/      # Endpoints y vistas Thymeleaf
└── config/          # Spring Security
```

## Flujo principal

1. Usuario entra → ve lista de **materias**
2. Elige una materia → ve las **cátedras** ordenadas por rating
3. Entra a una cátedra → ve las **reviews**
4. Se registra/loguea → puede **dejar su review** (1-5 estrellas + comentario)
