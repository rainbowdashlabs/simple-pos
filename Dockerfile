# --- Frontend ---
FROM node:26-alpine@sha256:6bd9ab43c0ec0190ca7b2d193906cb8b7edf6e7f4afa43ddeccd6452710bffb5 AS frontend

WORKDIR /build

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ .
RUN npm run build

# --- Backend ---
FROM eclipse-temurin:25-alpine AS backend

WORKDIR /build

COPY backend/gradlew ./
COPY backend/gradle/ gradle/
RUN chmod +x gradlew && ./gradlew --version

COPY backend/build.gradle.kts backend/settings.gradle.kts ./
COPY backend/gradle/libs.versions.toml gradle/libs.versions.toml
RUN ./gradlew dependencies --no-daemon || true

COPY backend/src/ src/
RUN ./gradlew bootJar -x test --no-daemon

# --- Runtime ---
FROM eclipse-temurin:25.0.5_8-jre-alpine

RUN addgroup -S app && adduser -S -G app -u 10001 app

WORKDIR /app

COPY --from=backend /build/build/libs/simple-pos*.jar app.jar
COPY --from=backend /build/src/main/resources/application.yaml application.yaml
COPY --from=frontend /build/dist/ /app/public/

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
