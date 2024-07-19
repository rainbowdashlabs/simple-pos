# THIS DOCKERFILE EXISTS FOR DEMONSTRATION PURPOSES
# IS HAS NO WORKING BACKEND WHATSOEVER

FROM nixos/nix:latest AS build

WORKDIR build/
RUN nix-channel --update
COPY shell.nix .

COPY frontend/package*.json /build
RUN nix-shell --run "npm ci"
COPY ./frontend/ .
RUN nix-shell --run "npm run build"

FROM nginx:alpine

COPY --from=build /build/dist /usr/share/nginx/html
