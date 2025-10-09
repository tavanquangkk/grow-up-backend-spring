# --- GIAI ĐOẠN BUILD ---
FROM gradle:jdk21 AS build # Hoặc tag Gradle/JDK nào bạn đang dùng thành công

WORKDIR /app

# Copy wrapper và build files
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source (có thể copy sau để test riêng biệt build files)
# COPY src ./src # Tạm thời comment dòng này nếu bạn muốn test build cơ bản trước

# Gán quyền thực thi cho gradlew (quan trọng!)
RUN chmod +x gradlew

# Build từng bước để debug
# Tạm thời thay thế dòng cleanBuildCache bằng một lệnh Gradle đơn giản hơn
# RUN ./gradlew cleanBuildCache

# Chỉnh sửa dòng này để in ra debug log:
# Thử chạy một lệnh Gradle đơn giản để kiểm tra môi trường
RUN ./gradlew tasks --debug # <-- Thay đổi ở đây để kiểm tra và lấy debug log

# Nếu lệnh trên thành công, hãy thử build lại với debug:
# RUN ./gradlew build -x test --no-daemon --debug # <-- Sử dụng dòng này sau khi `./gradlew tasks` thành công

# ... (Giữ nguyên các phần còn lại của Dockerfile)
# --- GIAI ĐOẠN CUỐI CÙNG (Final Stage) ---
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]