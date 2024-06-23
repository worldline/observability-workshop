#!/usr/bin/env bash

export OTEL_SERVICE_NAME=easypay-service
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_RESOURCE_ATTRIBUTES="source=agent"

export SERVER_PORT=8081
export LOGS_DIRECTORY="$(pwd)/logs"

java -Xms512m -Xmx512m -javaagent:$(pwd)/instrumentation/grafana-opentelemetry-java.jar -jar "$(pwd)/easypay-service/build/libs/easypay-service-0.0.1-SNAPSHOT.jar" "$@"
