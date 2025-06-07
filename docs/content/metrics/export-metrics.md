+++
date = '2025-06-07T15:53:56+02:00'
title = 'Export Metrics'
weight = 2
+++

## Expose metrics of our Java application

With Spring Boot, there are several ways to expose application metrics:

* The Spring Boot-way using the Actuator module relying on Micrometer,
* Using the Spring Boot OpenTelemetry Starter, but metric support is not as advanced as with Micrometer,
* Using OpenTelemetry library directly, but it needs more configuration,
* Using the OpenTelemetry Agent.

We will continue to use the OpenTelemetry Agent, as it is a straightforward way to collect metrics, and we already
configured it!

### Configure the OpenTelemetry Agent

The Agent should already collect metrics of the application, sending data every 60s.
That’s a bit long for our workshop, so we will reduce this frequency to 5s.

📝 Modify the `entrypoint` definition in the `compose.yml` file to add the following system properties:

```yaml
services:
  easypay-service:
    # ...
    entrypoint:
      - java
      - -javaagent:/opentelemetry-javaagent.jar
      - -Dotel.instrumentation.logback-appender.experimental-log-attributes=true
      - -Dotel.instrumentation.logback-appender.experimental.capture-mdc-attributes=*
      - -Dotel.metric.export.interval=5000 # <-- Add this line
      - -cp
      - app:app/lib/*
      - com.worldline.easypay.EasypayServiceApplication
```

ℹ️ The `otel.metric.export.interval` system property is used to define the frequency at which metrics are sent to the
target endpoint in milliseconds. As a reminder, you can also use environment variables to configure the Agent
(`otel.metric.export.interval` becomes `OTEL_METRIC_EXPORT_INTERVAL` environment variable name).

🛠️ Restart the easypay-service to take into account the new configuration:

```bash
docker compose up -d --build easypay-service
```


## Export metrics to Prometheus

Prometheus is a well-known time-series database and monitoring system that scrapes metrics from instrumented
applications. It even supports the OTLP protocol to ingest metrics.

Instead of sending them directly, we will keep to use the OpenTelemetry collector to collect metrics
and forward them to the target database.

👀 For this workshop, the OpenTelemetry collector is already configured to receive metrics and forward them to
Prometheus:

```yaml
# Input
receivers:
  # OpenTelemetry Protocol: logs, metrics and traces
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

#...
# Output
exporters:
  #...
  # Export to Prometheus via HTTP using the OpenTelemetry Protocol
  otlphttp/prometheus:
    endpoint: http://prometheus:9090/api/v1/otlp

# Telemetry processing pipelines
service:
  pipelines:
    # ...
    # Receive metrics using the OpenTelemetry Protocol and export to Prometheus
    metrics:
      receivers: [ otlp ]
      processors: [ batch ]
      exporters: [ otlphttp/prometheus ]
```