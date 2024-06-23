#!/usr/bin/env bash

export OTEL_SERVICE_NAME=smartbank-gateway
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_RESOURCE_ATTRIBUTES="source=agent"
export LOGS_DIRECTORY="$(pwd)/logs"

java -Xms512m -Xmx512m -javaagent:$(pwd)/instrumentation/grafana-opentelemetry-java.jar -jar "$(pwd)/smartbank-gateway/build/libs/smartbank-gateway-0.0.1-SNAPSHOT.jar" "$@"
