# Use a base image with Java
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the built JAR file (assuming you're using Maven)
COPY target/kafka_rain_processor.jar /app/kafka_rain_processor.jar

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/kafka_rain_processor.jar"]