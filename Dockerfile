FROM gradle:jdk21 AS build # <-- Thay đổi ở đây
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
# Sử dụng một JRE image nhẹ hơn để chạy ứng dụng
FROM openjdk:21-jre-slim
WORKDIR /app

# Copy JAR từ giai đoạn build
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port mà Spring Boot của bạn đang chạy (mặc định là 8080)
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]