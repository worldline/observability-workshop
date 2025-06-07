+++
date = '2025-06-07T16:11:48+02:00'
title = 'Sampling'
weight = 4
+++

When we instrument our services using the agent, every interaction, including Prometheus calls to the
`actuator/prometheus` endpoint, is recorded.

In the `Service Graph` you should have seen a link between the `User` and services other than the `api-gateway` it seems
not normal for us: we only created payments through the `api-gateway`!

👀 If you click on the link and select `View traces` you should see a lot of traces regarding `actuator/health`.

To avoid storing unnecessary data in Tempo, we can sample the data in two ways:

* [Head Sampling](https://opentelemetry.io/docs/concepts/sampling/#head-sampling)
* [Tail Sampling](https://opentelemetry.io/docs/concepts/sampling/#tail-sampling)

In this workshop, we will implement Tail Sampling, using the 
[tail_sampling processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/processor/tailsamplingprocessor/README.md).

Modify the OpenTelemetry Collector configuration file (``docker/otelcol/otelcol.yaml``) as follows:

```yaml
# ...
# Transform
processors:
  # ...
  tail_sampling/actuator: # Add a tail sampling definition for
      policies:
        [
          {
            name: "filter-http-url",
            type: "string_attribute",
            string_attribute: {
              key: "http.url",
              values: [ "/actuator/health" ],
              enabled_regex_matching: true,
              invert_match: true
            }
          },
          {
            name: "filter-url-path",
            type: "string_attribute",
            string_attribute: {
              key: "url.path",
              values: [ "/actuator/health" ],
              enabled_regex_matching: true,
              invert_match: true
            }
          }
        ]

# ...
# Telemetry processing pipelines
service:
  # Receive traces using the OpenTelemetry Protocol and export to Tempo
  traces:
    receivers: [ otlp ]
    processors: [ batch, tail_sampling/actuator ] # < add the tail_sampling processor here
    exporters: [ otlp/tempo ]
```

🛠️ Restart the collector:

```bash
$ docker compose up -d --build opentelemetry-collector
```

Starting from this moment, you should no longer see traces related to `actuator/health` endpoints.