# ============ Etapa 1: Build ============
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar el pom.xml primero (para aprovechar la caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y construir
COPY src ./src
RUN mvn clean package -DskipTests

# ============ Etapa 2: Runtime ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el jar construido
COPY --from=build /app/target/*.jar app.jar

# Render asigna el puerto dinámicamente mediante la variable PORT
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]