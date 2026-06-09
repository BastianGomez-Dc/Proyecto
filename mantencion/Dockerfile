FROM gradle:jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon -q 2>/dev/null || true
COPY src/ src/
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jdk-noble
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
