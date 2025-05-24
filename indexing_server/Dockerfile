# Minimal Runtime Image
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy only the prebuilt JAR
COPY target/*.jar app.jar

# Optional: mount a volume if you need access to Parquet files
VOLUME ["/parquet"]
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
