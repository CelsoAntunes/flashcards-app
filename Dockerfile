# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR from Maven into the container
COPY target/flashcards-0.0.1-SNAPSHOT.jar /app/flashcards-app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "flashcards-app.jar"]