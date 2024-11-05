# Use a base image for Java
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR file
COPY ./target/core-0.0.1-SNAPSHOT.jar app.jar

# Copy the static files to the container
#COPY ./src/main/resources/static /app/static

# Expose the port
EXPOSE 8080
#EXPOSE 8443

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]