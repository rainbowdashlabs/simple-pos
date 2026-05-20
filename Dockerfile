# --- Frontend ---
FROM node:22-alpine AS frontend

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
FROM eclipse-temurin:25.0.3_9-jre-alpine

ARG GITHUB_ACTIONS=false
ARG GITHUB_REF_TYPE=null
ARG GITHUB_REF_NAME=null
ARG GITHUB_SHA=null
ENV GITHUB_ACTIONS=$GITHUB_ACTIONS
ENV GITHUB_REF_TYPE=$GITHUB_REF_TYPE
ENV GITHUB_REF_NAME=$GITHUB_REF_NAME
ENV GITHUB_SHA=$GITHUB_SHA

WORKDIR /app

COPY --from=backend /build/build/libs/simple-pos*.jar app.jar
COPY --from=backend /build/src/main/resources/application.yaml application.yaml
COPY --from=frontend /build/dist/ /app/public/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
