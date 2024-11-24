# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml and download dependencies (to cache them)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a slim Java image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file and static resources from the build stage
COPY --from=build /app/target/core-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/src/main/resources/static /app/static

#Copy Keystore for HTTPS
COPY --from=build /app/src/main/resources/keystore.p12 /app/keystore.p12

# Copy application-simulated.properties for the simulated profile
COPY --from=build /app/src/main/resources/application-simulated.properties /app/application-simulated.properties

# Expose the port(s)
#EXPOSE 8080
EXPOSE 8443

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=simulated", "-jar", "app.jar"]
