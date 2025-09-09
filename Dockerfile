FROM gradle:jdk22-focal AS builder
WORKDIR /home/gradle/src
COPY . .
RUN gradle bootJar --no-daemon

FROM openjdk:22-jre-slim
WORKDIR /app
COPY --from=builder /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]