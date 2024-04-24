FROM node:20.11.1 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install -g rollup
RUN npm install

COPY . .
ARG BACKEND_URL
ENV BACKEND_URL=$BACKEND_URL

RUN npm run build

FROM nginx:1.19

COPY --from=build /app/public /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf