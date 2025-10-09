FROM openjdk:17-jdk-alpine
COPY build/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080