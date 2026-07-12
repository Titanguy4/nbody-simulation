FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Cache Gradle distribution and dependencies before copying sources
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src ./src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --no-create-home nbody
USER nbody

COPY --from=build /app/build/libs/*.jar app.jar

ENV MQTT_BROKER_URL=tcp://mosquitto:1883

ENTRYPOINT ["java", "-jar", "app.jar"]
