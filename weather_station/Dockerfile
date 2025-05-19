# ---------- Stage 1: Build ----------
    FROM maven:3.9.6-eclipse-temurin-17 AS builder

    WORKDIR /app
    COPY pom.xml .
    COPY src ./src
    
    # Build the JAR file
    RUN mvn clean package -DskipTests
    
    # ---------- Stage 2: Run ----------
    FROM eclipse-temurin:17-jdk
    
    WORKDIR /app
    # Copy only the built jar from the builder stage
    COPY --from=builder /app/target/*.jar app.jar
    
    # Run the application
    ENTRYPOINT ["java", "-jar", "app.jar"]
    