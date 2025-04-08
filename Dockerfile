# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Maven or Gradle build artifact into the container
COPY target/flashcards-app.jar /app/flashcards-app.jar

# Expose the port the app runs on
EXPOSE 8080

# Use the generated .jar file name (flashcards-0.0.1-SNAPSHOT.jar)
COPY target/flashcards-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "flashcards-app.jar"]