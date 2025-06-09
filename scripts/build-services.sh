#!/usr/bin/env bash

for service in {"config-server","discovery-server","api-gateway","smartbank-gateway","fraudetect-service","merchant-backoffice"}; do
    echo "Building service: $service"
    docker compose build "$service"
done