FROM eclipse-temurin:22-jre-jammy
WORKDIR /app

EXPOSE 8080

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]