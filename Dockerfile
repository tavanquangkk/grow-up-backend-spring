# --- Build stage ---
FROM gradle:8.3.3-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper & build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src ./src

# Build fat jar
RUN ./gradlew build -x test

# --- Run stage ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy built jar từ build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
