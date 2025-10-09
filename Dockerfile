# --- GIAI ĐOẠN BUILD ---
# Sử dụng image Gradle/JDK tồn tại (ví dụ: gradle:jdk21)
FROM gradle:jdk21 AS build

WORKDIR /app

# Copy wrapper và build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source
COPY src ./src

# Build
RUN ./gradlew cleanBuildCache
RUN ./gradlew build -x test --no-daemon

# --- GIAI ĐOẠN CUỐI CÙNG (Final Stage) ---
# Sử dụng một JRE image nhẹ hơn và TỒN TẠI trên Docker Hub
FROM openjdk:21-jre-slim-bullseye
WORKDIR /app

# Copy JAR từ giai đoạn build
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port mà Spring Boot của bạn đang chạy (mặc định là 8080)
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]