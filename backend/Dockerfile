# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies first
COPY pom.xml .
RUN mvn -q dependency:go-offline
# Build the app
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Non-root user
RUN useradd -ms /bin/bash appuser
COPY --from=build /app/target/*.jar app.jar
# Uploads directory (mount a volume here in production)
RUN mkdir -p /app/uploads && chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
