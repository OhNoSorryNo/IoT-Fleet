# -----------------------------------------
# 1. STAGE: Build mit Maven
# -----------------------------------------
FROM maven:3.8.5-openjdk-17 AS build
LABEL stage=builder

WORKDIR /app

# Nur pom.xml kopieren und Abhängigkeiten herunterladen (für Docker-Caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Quellcode kopieren
COPY src ./src

# Package (ohne Tests) erstellen
RUN mvn clean package -DskipTests

# -----------------------------------------
# 2. STAGE: Minimales Runtime-Image
# -----------------------------------------
FROM openjdk:17-jdk-alpine

WORKDIR /app

# Das fertig gebaute JAR aus dem ersten Stage kopieren
COPY --from=build /app/target/*.jar iot-app.jar

# Port freigeben (Falls Deine Anwendung Default-HTTP-Port 8080 verwendet)
EXPOSE 8080

# Beispiel-ENV-Variablen
ENV DEVICE_ID=SimulatedDevice123
ENV SECRET_KEY=MySecretKey

# ENTRYPOINT ersetzt die entrypoint.sh-Logik:
# - Nutzt die Umgebungsvariable SPRING_PROFILES_ACTIVE oder 'default'
# - Setzt SECRET_TOKEN, falls angegeben
ENTRYPOINT sh -c "java \
  -Dspring.profiles.active=\"\${SPRING_PROFILES_ACTIVE:-default}\" \
  -DSECRET_TOKEN=\"\${SECRET_TOKEN}\" \
  -jar /app/iot-app.jar"
