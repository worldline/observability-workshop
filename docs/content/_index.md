+++
title = "Observability Worshop"
type = "home"
+++

> Observability from Development: Master Your Java Applications in Production with Open Telemetry

This workshop aims to introduce how to make a Java application fully observable with:

* Logs with insightful information
* Metrics with [Prometheus](https://prometheus.io/)
* [Distributed Tracing](https://blog.touret.info/2023/09/05/distributed-tracing-opentelemetry-camel-artemis/)

During this workshop we will use OpenTelemetry and a Grafana stack:

* [Grafana](https://grafana.com/): for dashboards
* [Loki](https://grafana.com/oss/loki/): for storing our logs
* [Tempo](https://grafana.com/oss/tempo/): for storing traces
* [Prometheus](https://prometheus.io/): for gathering and storing metrics.

We will also cover the:

* OpenTelemetry Protocol (OTLP) to send our telemetry data over the network,
* [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/) which gathers & broadcasts
  then the data coming from our microservices,
* And obviously the OpenTelemetry Java instrumentation to collect all the telemetry data of our application.

## Start with [Workshop Overview](./workshop-overview/_index.md)