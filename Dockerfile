# Stage 1: Build stage (Using Ubuntu-based image for better compatibility with Gradle)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle files
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Download dependencies (caching)
RUN ./gradlew build -x test --no-daemon || return 0

# Copy source code
COPY src src

# Build application
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime stage (Keeping it light with Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install libstdc++ and gcompat for potential native dependencies at runtime
RUN apk add --no-cache libstdc++ gcompat

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]