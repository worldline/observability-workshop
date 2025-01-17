FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /workspace/app

ENV SERVICE=config-server

COPY .gradle .gradle
COPY ${SERVICE} ${SERVICE}
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/root/.gradle ./gradlew :${SERVICE}:clean :${SERVICE}:build
RUN mkdir -p ${SERVICE}/build/dependency && (cd ${SERVICE}/build/dependency && jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:21-jre-alpine

ENV SERVICE=config-server
ENV LOGS_DIRECTORY=/logs/

ARG UID=1000
ARG GID=1000

RUN apk add --no-cache curl

RUN addgroup --system --gid ${GID} javauser && adduser -S -s /usr/sbin/nologin --uid ${UID} -G javauser javauser

ARG DEPENDENCY=/workspace/app/${SERVICE}/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

RUN chown -R javauser:javauser /app

USER javauser

ENTRYPOINT ["java","-cp","app:app/lib/*","com.worldline.easypay.configserver.ConfigServerApplication"]