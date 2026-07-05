# --- Build stage: compile and package the Spring Boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first (only re-downloads when pom.xml changes).
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# --- Runtime stage: small JRE image with just the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# The app reads $PORT (defaults to 8080). Hosting platforms set it automatically.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
