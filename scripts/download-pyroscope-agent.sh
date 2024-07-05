#!/usr/bin/env bash

PYROSCOPE_VERSION=v0.14.0
PYROSCOPE_OTEL_VERSION=0.10.1.11

# Define the URL of the Grafana OpenTelemetry agent
PYROSCOPE_URL="https://github.com/grafana/pyroscope-java/releases/download/${PYROSCOPE_VERSION}/pyroscope.jar"
PYROSCOPE_OTEL_URL="https://repo1.maven.org/maven2/io/pyroscope/otel/${PYROSCOPE_OTEL_VERSION}/otel-${PYROSCOPE_OTEL_VERSION}.jar"

# Define the path to the instrumentation directory
#INSTRUMENTATION_DIR="$(dirname "$(dirname "${BASH_SOURCE[0]}")")/instrumentation"

INSTRUMENTATION_DIR="$(dirname "${BASH_SOURCE[0]}")/../instrumentation"

# Create the instrumentation directory if it doesn't exist
mkdir -p "$INSTRUMENTATION_DIR"

# Download the Grafana OpenTelemetry agent and save it in the instrumentation directory
curl -L "$PYROSCOPE_URL" -o "$INSTRUMENTATION_DIR/pyroscope.jar"
curl -L "$PYROSCOPE_OTEL_URL" -o "$INSTRUMENTATION_DIR/pyroscope-otel.jar"

# Print a success message
echo "Grafana Pyroscope agent downloaded successfully in $INSTRUMENTATION_DIR"