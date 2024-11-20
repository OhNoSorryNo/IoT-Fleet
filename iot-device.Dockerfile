# Stage 1: Build the IoT device application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies (to cache them)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the IoT device using a slim Java image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the IoT Device JAR file from the build stage
COPY --from=build /app/target/iot-device-0.0.1-SNAPSHOT.jar app.jar

# Configuration via environment variables
ENV SERVER_URL=https://app:8443
ENV AGENT_ID=mock-device-1
ENV SECRET_KEY=mock-key
ENV PING_FREQUENCY=5000

# Run the IoT device application
ENTRYPOINT ["java", "-jar", "app.jar"]
