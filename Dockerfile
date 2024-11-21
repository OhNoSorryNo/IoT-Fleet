# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a slim Java image
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/core-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
