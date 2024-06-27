FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /workspace/app

ENV SERVICE=easypay-service

COPY .gradle .gradle
COPY ${SERVICE} ${SERVICE}
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/root/.gradle ./gradlew :${SERVICE}:clean :${SERVICE}:build -x test
RUN mkdir -p ${SERVICE}/build/dependency && (cd ${SERVICE}/build/dependency && jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:21-jre-alpine

ENV SERVICE=easypay-service
ENV LOGS_DIRECTORY=/logs/

ARG UID=1000
ARG GID=1000

RUN addgroup --system --gid ${GID} javauser && adduser -S -s /usr/sbin/nologin --uid ${UID} -G javauser javauser

ARG DEPENDENCY=/workspace/app/${SERVICE}/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

RUN chown -R javauser:javauser /app

RUN apk add --no-cache curl

USER javauser

COPY instrumentation/grafana-opentelemetry-java.jar /app/grafana-opentelemetry-java.jar

ENTRYPOINT ["java", "-javaagent:/app/grafana-opentelemetry-java.jar", "-cp","app:app/lib/*","com.worldline.easypay.EasypayServiceApplication"]