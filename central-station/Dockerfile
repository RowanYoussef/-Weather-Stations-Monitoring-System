FROM openjdk:21-jdk-slim
WORKDIR /app

COPY target/*.jar app.jar

VOLUME ["/parquet"]
EXPOSE 8080

ENTRYPOINT ["java", "-XX:StartFlightRecording=duration=1m,filename=/app/central_station.jfr,settings=profile", "-jar", "app.jar"]
