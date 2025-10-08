# --- Build stage ---
FROM gradle:8.3-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper + build files
COPY gradlew .
COPY gradle ./gradle                 # đây phải copy cả thư mục wrapper
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code
COPY src ./src

# Make wrapper executable
RUN chmod +x gradlew

# Build fat jar
RUN ./gradlew build -x test --stacktrace

# --- Run stage ---
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy built jar từ build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
