# 1. Build Stage
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Nur pom.xml kopieren und Abhängigkeiten herunterladen (Docker Caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Quellcode kopieren und bauen
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Runtime Stage
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Das JAR aus dem Build-Stage kopieren
COPY --from=build /app/target/*.jar iot-app.jar

# Port freigeben, falls notwendig (z.B. für Swagger UI)
EXPOSE 8080

# Umgebungsvariablen für die Kommunikation
ENV DEVICE_ID=SimulatedDevice123
ENV SECRET_KEY=MySecretKey

ENTRYPOINT ["java", "-jar", "iot-app.jar"]
