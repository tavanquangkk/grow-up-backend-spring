# --- GIAI ĐOẠN BUILD ---
FROM gradle:jdk21 AS build

WORKDIR /app

# Copy wrapper và build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source
COPY src ./src

# Gán quyền thực thi cho gradlew
RUN chmod +x gradlew

# Build
RUN ./gradlew cleanBuildCache
RUN ./gradlew build -x test --no-daemon

# --- THÊM DÒNG NÀY ĐỂ DEBUG ---
RUN ls -l /app/build/libs/ # <-- THÊM DÒNG NÀY VÀO CUỐI GIAI ĐOẠN BUILD

# --- GIAI ĐOẠN CUỐI CÙNG (Final Stage) ---
FROM openjdk:21-jdk-slim
WORKDIR /app

# Dòng này sẽ được chỉnh sửa sau khi bạn có tên JAR chính xác
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]