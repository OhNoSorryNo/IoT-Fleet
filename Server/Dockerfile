FROM maven:3.9.8-eclipse-temurin-21 AS build
LABEL stage=builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar iot-app.jar

EXPOSE 8080

ENV DEVICE_ID=SimulatedDevice123
ENV SECRET_KEY=MySecretKey

ENTRYPOINT sh -c "java \
  -Dspring.profiles.active=\"\${SPRING_PROFILES_ACTIVE:-default}\" \
  -DSECRET_TOKEN=\"\${SECRET_TOKEN}\" \
  -jar /app/iot-app.jar"
