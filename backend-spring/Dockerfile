FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/cookflow-backend-0.1.0-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
