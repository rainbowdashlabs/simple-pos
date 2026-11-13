# --- Frontend ---
FROM node:24-alpine@sha256:116b800c3754b440ee0aa3ed2f20f5bf3d99a554bfd9adeea09ea8017f6e85f5 AS frontend

WORKDIR /build

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ .
RUN npm run build

# --- Backend ---
FROM eclipse-temurin:25-alpine@sha256:c7c3d0bdf7f2bbc5a05a6712ed334d198360105634ea9adf76fca94f381b1e3f AS backend

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
FROM eclipse-temurin:25.0.5_8-jre-alpine@sha256:e1898fd6a11fc965a994d8ace186fd3585aeb636cf8f05f47a723fcfee0b0918

RUN addgroup -S app && adduser -S -G app -u 10001 app

WORKDIR /app

COPY --from=backend /build/build/libs/simple-pos*.jar app.jar
COPY --from=backend /build/src/main/resources/application.yaml application.yaml
COPY --from=frontend /build/dist/ /app/public/

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
