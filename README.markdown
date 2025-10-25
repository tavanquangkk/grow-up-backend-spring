# Grow-Up App

A Spring Boot application with JWT authentication and PostgreSQL, containerized with Docker and deployable to free cloud platforms (e.g., Render).

## Prerequisites
- **Java**: 17+
- **Gradle**: For building
- **Docker**: For containerization
- **PostgreSQL**: 17 (local or managed)
- **OpenSSL**: For JWT keys
- **Git**: For version control

## Setup Instructions

### 1. Database (Local PostgreSQL)
1. **Install PostgreSQL 17 (macOS)**:
   ```bash
   brew install postgresql@17
   brew services start postgresql@17
   ```
2. **Create Database**:
   ```sql
   CREATE DATABASE growup_db;
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE growup_db TO postgres;
   ```
3. **Configure `application.properties`**:
   ```properties
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.datasource.url=jdbc:postgresql://localhost:5432/growup_db
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   ```

### 2. JWT Key Generation
1. **Create Keys Directory**:
   ```bash
   mkdir -p src/main/resources/keys
   ```
2. **Generate Keys**:
   ```bash
   openssl genpkey -algorithm RSA -out src/main/resources/keys/private-key.pem -pkeyopt rsa_keygen_bits:2048
   openssl rsa -pubout -in src/main/resources/keys/private-key.pem -out src/main/resources/keys/public-key.pem
   ```
3. **Configure JWT** (`application.properties`):
   ```properties
   jwt.private-key=classpath:keys/private-key.pem
   jwt.public-key=classpath:keys/public-key.pem
   jwt.issuer=grow-up-app
   jwt.expiration=900000  # 15 minutes
   ```

### 3. Docker Setup
1. **Create `Dockerfile`**:
   ```dockerfile
   FROM openjdk:17-jdk-slim
   WORKDIR /app
   COPY build/libs/grow-up-app.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```
2. **Create `docker-compose.yml`** (Local Testing):
   ```yaml
   version: '3.8'
   services:
     app:
       build: .
       ports:
         - "8080:8080"
       depends_on:
         - db
       environment:
         - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/growup_db
         - SPRING_DATASOURCE_USERNAME=postgres
         - SPRING_DATASOURCE_PASSWORD=postgres
         - JWT_PRIVATE_KEY=/app/keys/private-key.pem
         - JWT_PUBLIC_KEY=/app/keys/public-key.pem
       volumes:
         - ./src/main/resources/keys:/app/keys
     db:
       image: postgres:17
       environment:
         - POSTGRES_DB=growup_db
         - POSTGRES_USER=postgres
         - POSTGRES_PASSWORD=postgres
       ports:
         - "5432:5432"
   ```
3. **Build and Run Locally**:
   ```bash
   ./gradlew build
   docker-compose up --build
   ```
4. **Access**: `http://localhost:8080`



## Notes
- Ensure `keys/` and `.pem` files have read permissions.
- Use migrations (Flyway/Liquibase) for production instead of `spring.jpa.hibernate.ddl-auto=update`.
- Test thoroughly on free platforms due to resource limits.
