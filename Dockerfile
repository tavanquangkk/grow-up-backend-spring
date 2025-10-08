FROM gradle:8.3-jdk17 AS build
WORKDIR /app

# Copy wrapper và build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source
COPY src ./src

# Build
RUN ./gradlew build -x test --no-daemon
