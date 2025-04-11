FROM eclipse-temurin:21-jdk-alpine AS build

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

ENV OTEL_AGENT_VERSION "v2.14.0"
ENV OTEL_AGENT_URL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar"

ADD --chown=$UID:$GID ${OTEL_AGENT_URL} /opentelemetry-javaagent.jar

ENV PYROSCOPE_VERSION=v2.0.0
ENV PYROSCOPE_URL="https://github.com/grafana/pyroscope-java/releases/download/${PYROSCOPE_VERSION}/pyroscope.jar"
ENV PYROSCOPE_OTEL_VERSION=v1.0.1
ENV PYROSCOPE_OTEL_URL="https://github.com/grafana/otel-profiling-java/releases/download/${PYROSCOPE_OTEL_VERSION}/pyroscope-otel.jar"

ADD --chown=$UID:$GID ${PYROSCOPE_URL} /pyroscope.jar
ADD --chown=$UID:$GID ${PYROSCOPE_OTEL_URL} /pyroscope-otel.jar

ENTRYPOINT ["java", "-cp","app:app/lib/*","com.worldline.easypay.EasypayServiceApplication"]