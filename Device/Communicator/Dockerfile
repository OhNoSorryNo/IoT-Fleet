# -------- STAGE 1: Build the application using Maven --------
FROM maven:3.9.8-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven configuration file (pom.xml) and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# -------- STAGE 2: Run the JAR file using a slim Java image --------
FROM openjdk:17-jdk-slim

# Set the working directory for the final container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port (8080) or your custom port
EXPOSE 8282

# If needed, specify an active Spring profile here (e.g., "simulated")
# ENV SPRING_PROFILES_ACTIVE=simulated

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
