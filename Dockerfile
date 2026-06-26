# ===== Etapa 1: compilar el .jar con Maven y Java 17 =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copiamos primero el pom para cachear las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Ahora el codigo y empaquetamos (sin tests)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Etapa 2: imagen liviana solo con el runtime de Java =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render inyecta la variable PORT; la app la respeta (ver application.properties)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
