#!/usr/bin/env bash

VERSION=v2.4.0-beta.1

# Define the URL of the Grafana OpenTelemetry agent
AGENT_URL="https://github.com/grafana/grafana-opentelemetry-java/releases/download/${VERSION}/grafana-opentelemetry-java.jar"

# Define the path to the instrumentation directory
#INSTRUMENTATION_DIR="$(dirname "$(dirname "${BASH_SOURCE[0]}")")/instrumentation"

INSTRUMENTATION_DIR="$(dirname "${BASH_SOURCE[0]}")/../instrumentation"

# Create the instrumentation directory if it doesn't exist
mkdir -p "$INSTRUMENTATION_DIR"

# Download the Grafana OpenTelemetry agent and save it in the instrumentation directory
curl -L "$AGENT_URL" -o "$INSTRUMENTATION_DIR/grafana-opentelemetry-java.jar"

# Print a success message
echo "Grafana OpenTelemetry agent downloaded successfully in $INSTRUMENTATION_DIR"