+++
date = '2025-06-07T16:05:12+02:00'
title = 'Export'
weight = 2
+++

## Enable distributed tracing

To capture the entire transaction across all services in a trace, it's essential to instrument all the services in our
application.

> [!IMPORTANT]
> In this workshop, our primary focus keeps to be on the `easypay` service.
> For efficiency, we have already instrumented the other services beforehand.

We have already configured the OpenTelemetry Java Agent to transmit telemetry data directly to the collector, so our 
application have already sent traces to the collector.

#### OpenTelemetry Collector

OpenTelemetry Collector is already configured to accept traces through the
OpenTelemetry GRPC protocol (OTLP) on port `4317`, and then forward them to *Grafana Tempo*, which listens on the host
`tempo` on the same port `4317` (this setup specifically handles OTLP traces).

👀 You can have a look at the collector configuration located in the `docker/otelcol/otelcol.yaml` file:

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
  # ...
  # Export to Tempo via GRPC using the OpenTelemetry Protocol
  otlp/tempo:
    endpoint: tempo:4317
    tls:
      insecure: true

# Telemetry processing pipelines
service:
  pipelines:
    # ...
    # Receive traces using the OpenTelemetry Protocol and export to Tempo
    traces:
        receivers: [ otlp ]
        processors: [ batch ]
        exporters: [ otlp/tempo ]
```