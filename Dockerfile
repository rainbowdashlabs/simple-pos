FROM node:22 AS build

WORKDIR build/

ENV PATH /build/node_modules/.bin:$PATH

COPY package*.json /build
#RUN npm install
RUN npm ci --include dev --include prod

COPY . .

#ENTRYPOINT ["sh"]

RUN npm run build

#FROM nginx:alpine

#COPY --from=build /dist /usr/share/nginx/html
