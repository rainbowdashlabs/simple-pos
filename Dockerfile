FROM nixos/nix:latest AS frontend

WORKDIR build/
RUN nix-channel --update
COPY shell.nix .

COPY frontend/package*.json /build
RUN nix-shell --run "npm ci"
COPY ./frontend/ .
RUN nix-shell --run "npm run build"

FROM gradle:jdk21-alpine AS spring

COPY backend/ .
RUN rm src/main/resources/public -rf
COPY --from=frontend /build/dist/ ./src/main/resources/public

RUN gradle bootJar -x test

FROM eclipse-temurin:21-alpine

WORKDIR /app

COPY --from=spring /home/gradle/build/libs/simple-pos*.jar app.jar
COPY --from=spring /home/gradle/src/main/resources/application.yaml application.yaml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
