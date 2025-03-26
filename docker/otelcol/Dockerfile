ARG VERSION=0.122.0

FROM otel/opentelemetry-collector-contrib:${VERSION}

COPY otelcol.yaml /etc/otelcol/config.yaml

CMD ["--config", "/etc/otelcol/config.yaml"]