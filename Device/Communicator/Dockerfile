FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar iot-app.jar
COPY src/main/resources/certs/server_cert.pem /app/certs/server_cert.pem

EXPOSE 8080

ENV DEVICE_ID=SimulatedDevice123
ENV SECRET_KEY=MySecretKey
ENV SPRING_PROFILES_ACTIVE=simulated
ENV BACKEND_URL=https://server-app:8443/agents

ENTRYPOINT ["java", "-jar", "iot-app.jar"]
