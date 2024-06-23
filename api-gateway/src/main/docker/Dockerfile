FROM eclipse-temurin:21-jdk-alpine as build

ENV USE_SYSTEM_CA_CERTS=1

# INSTALL CA Certificates (useful for companies with SSL inspection)
ADD --chmod=644 docker/ca-trust/* /certificates/
RUN export USE_SYSTEM_CA_CERTS=1 && /__cacert_entrypoint.sh

WORKDIR /workspace/app

ENV SERVICE=api-gateway

COPY .gradle .gradle
COPY ${SERVICE} ${SERVICE}
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/root/.gradle /__cacert_entrypoint.sh ./gradlew :${SERVICE}:clean :${SERVICE}:build -x test
RUN mkdir -p ${SERVICE}/build/dependency && (cd ${SERVICE}/build/dependency && jar -xf ../libs/*-SNAPSHOT.jar)

FROM eclipse-temurin:21-jre-alpine

# INSTALL CA Certificates (useful for companies with SSL inspection)
ADD --chmod=644 docker/ca-trust/* /certificates/
RUN export USE_SYSTEM_CA_CERTS=1 && /__cacert_entrypoint.sh

ENV SERVICE=api-gateway
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

ENTRYPOINT ["java","-javaagent:/app/grafana-opentelemetry-java.jar","-cp","app:app/lib/*","com.worldline.easypay.apigateway.ApiGatewayApplication"]