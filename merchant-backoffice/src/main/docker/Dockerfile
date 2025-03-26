FROM eclipse-temurin:21-jdk-alpine as build

WORKDIR /workspace/app

ENV SERVICE=merchant-backoffice

COPY .gradle .gradle
COPY ${SERVICE} ${SERVICE}
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/root/.gradle ./gradlew :${SERVICE}:clean :${SERVICE}:build -x test
RUN mkdir -p ${SERVICE}/build/dependency && (cd ${SERVICE}/build/dependency && jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:21-jre-alpine

ENV SERVICE=merchant-backoffice
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

ENTRYPOINT ["java", "-cp","app:app/lib/*","com.worldline.easypay.merchantbo.MerchantBackofficeApplication"]