FROM gradle:7-jdk21

WORKDIR /api
COPY . .

CMD ["./gradlew","bootRun"]
