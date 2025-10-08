Grow-Up App
A Spring Boot application with JWT authentication, PostgreSQL database, and Cloudinary integration for media storage. This project is containerized using Docker and deployable to free cloud platforms.
Table of Contents

Prerequisites
Database Setup
JWT Key Generation
Configuration
Running Locally
Docker Setup
Deploying to Free Platforms
JWT Usage
Notes

Prerequisites

Java: 17+
Gradle: For dependency management
PostgreSQL: 17
OpenSSL: For JWT key generation
Cloudinary: Account for media storage (optional)
Docker: For containerization
Git: For version control and deployment
Database Setup

Install PostgreSQL 17 (macOS):
bashbrew install postgresql@17
brew services start postgresql@17

Create Database and User:
sqlCREATE DATABASE growup_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE growup_db TO postgres;

Update application.properties:
propertiesspring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/growup_db
spring.datasource.username=postgres
spring.datasource.password=postgres


JWT Key Generation

Create Keys Directory:
bashmkdir -p src/main/resources/keys

Generate Private Key:
bashopenssl genpkey -algorithm RSA -out src/main/resources/keys/private-key.pem -pkeyopt rsa_keygen_bits:2048

Extract Public Key:
bashopenssl rsa -pubout -in src/main/resources/keys/private-key.pem -out src/main/resources/keys/public-key.pem

Verify Files:
bashls -l src/main/resources/keys/
# Should show: private-key.pem  public-key.pem


Configuration

JWT Configuration (application.properties):
propertiesjwt.private-key=classpath:keys/private-key.pem
jwt.public-key=classpath:keys/public-key.pem
jwt.issuer=grow-up-app
jwt.expiration=900000  # 15 minutes in milliseconds

Cloudinary Configuration (Optional):
propertiescloudinary.cloud_name=YOUR_CLOUD_NAME
cloudinary.api_key=YOUR_API_KEY
cloudinary.api_secret=YOUR_API_SECRET

JPA/Hibernate Configuration:
propertiesspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true


Running Locally

Build and Run:
bash./gradlew bootRun  # Gradle

Access: Application runs on http://localhost:8080.

Docker Setup

Create Dockerfile for Spring Boot:
dockerfileFROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/grow-up-app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

Create docker-compose.yml for Local Testing:
yamlversion: '3.8'
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
volumes:
- pgdata:/var/lib/postgresql/data
volumes:
pgdata:

Build and Run Locally:
bash./gradlew build
docker-compose up --build

Test: Access http://localhost:8080.

Deploying to Free Platforms
The following platforms support free deployment of Dockerized Spring Boot apps with PostgreSQL. Render is recommended for simplicity.
Render (Recommended)

Free Tier: 750 hours/month for Web services, PostgreSQL with 90GB storage (90-day trial).
Steps:

Push code to GitHub (include Dockerfile, docker-compose.yml optional).
Sign up at render.com with GitHub.
Create Web Service:

New > Web Service > Connect GitHub repo.
Runtime: Docker.
Build Command: ./gradlew build.
Start Command: java -jar build/libs/grow-up-app.jar.


Create PostgreSQL:

New > PostgreSQL > Select free tier.
Copy DB URL (e.g., postgresql://user:pass@host:port/db).


Set Environment Variables:
plaintextSPRING_DATASOURCE_URL=<Render Postgres URL>
SPRING_DATASOURCE_USERNAME=<Postgres User>
SPRING_DATASOURCE_PASSWORD=<Postgres Password>
JWT_PRIVATE_KEY=<Paste private-key.pem content>
JWT_PUBLIC_KEY=<Paste public-key.pem content>
CLOUDINARY_CLOUD_NAME=<Your Cloudinary Name>
CLOUDINARY_API_KEY=<Your API Key>
CLOUDINARY_API_SECRET=<Your API Secret>

Deploy: Render auto-builds and deploys. Access via provided URL.


Limitations: Idle services sleep after 15 minutes; DB free for 90 days.

Railway

Free Tier: $5/month credit (~500 hours), 500MB DB storage.
Steps:

Push to GitHub.
Sign up at railway.app with GitHub.
New Project > Deploy from GitHub > Select repo.
Add PostgreSQL: New > Database > PostgreSQL.
Set Variables (same as Render).
Deploy: Auto-builds Docker image.


Limitations: Credit-based; stops if exceeded.

Fly.io

Free Tier: 3 VMs (256MB RAM), 1GB Postgres.
Steps:

Install flyctl: curl -L https://fly.io/install.sh | sh.
Sign up: flyctl auth signup.
Initialize: fly launch (detects Dockerfile).
Add Postgres: fly postgres create.
Set Secrets:
bashfly secrets set SPRING_DATASOURCE_URL=<Postgres URL> ...

Deploy: fly deploy.


Limitations: Low RAM; optimize with Quarkus/GraalVM.

JWT Usage

Generate Access Token:
javaString token = jwtUtil.generateToken(user);

Generate Refresh Token:
javaString refreshToken = jwtUtil.generateRefreshToken(user);

Validate Token:
javaboolean isValid = jwtUtil.validateToken(token, userDetails);

Extract Username:
javaString username = jwtUtil.extractUsername(token);


Notes

Ensure keys/ directory and private-key.pem, public-key.pem have read permissions.
spring.jpa.hibernate.ddl-auto=update is for development; use migrations (Flyway/Liquibase) for production.
JWT expiration is in milliseconds (900000 = 15 minutes).
For Cloudinary, secure API keys via environment variables.
Free platforms may sleep or have limited resources; test thoroughly before showcasing in portfolio.