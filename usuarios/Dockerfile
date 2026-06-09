FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

ENV GRADLE_VERSION=9.4.1
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget unzip && \
    wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -O /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/gradle.zip

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon -q 2>/dev/null || true

COPY src/ src/
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:25-jdk-noble
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
