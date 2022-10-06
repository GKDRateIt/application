FROM openjdk:17-slim-bullseye

ARG BUILD_VER
ENV BUILD_VER $BUILD_VER
COPY ./build/libs/rate-it-backend-${BUILD_VER}-shadow.jar /app/bin/

WORKDIR /app/bin/

CMD ["java", "-jar", "/app/bin/rate-it-backend-${BUILD_VER}-shadow.jar", "/app/config/config.json"]