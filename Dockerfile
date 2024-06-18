# Use the official OpenJDK 17 image as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project (including the Gradle files) into the container
COPY . .


# Expose the port that your Spring Boot application listens on
EXPOSE 8080

# Set the entry point to run the compiled JAR file
ENTRYPOINT ["java", "-jar", "notes.jar"]
