FROM oven/bun:1 AS build
WORKDIR /app

COPY package.json bun.lock tsconfig.json ./
RUN bun install --frozen-lockfile

COPY index.html ./
COPY css ./css
COPY src ./src
RUN bun run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
