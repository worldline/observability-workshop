+++
date = '2025-06-07T16:17:35+02:00'
title = 'Metrics and Traces'
weight = 3
+++

Exemplars are annotations used in metrics that link specific occurrences, like logs or traces, to data points within a metric time series. 
They provide direct insights into system behaviors at moments captured by the metric, aiding quick diagnostics by showing related trace data where anomalies occur. 
This feature is valuable for debugging, offering a clearer understanding of metrics in relation to system events.

🛠️ Generate some load towards the `easypay-service`:

```bash
$ k6 run -u 1 -d 2m k6/01-payment-only.js
```

ℹ️ Exemplars are tied to a metric and contain a trace ID and a span ID. They are especially available in the OpenMetrics 
format, but also OpenTelemetry support them. In OpenMetrics format, they appear as follows:

```
http_server_requests_seconds_bucket{application="easypay-service",error="none",exception="none",instance="easypay-service:39a9ae31-f73a-4a63-abe5-33049b8272ca",method="GET",namespace="local",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.027962026"} 1121 # {span_id="d0cf53bcde7b60be",trace_id="969873d828346bb616dca9547f0d9fc9"} 0.023276118 1719913187.631
```

The interesting part starts after the `#` character, this is the so-called exemplar:

```
               SPAN ID                           TRACE ID                     VALUE      TIMESTAMP
# {span_id="d0cf53bcde7b60be",trace_id="969873d828346bb616dca9547f0d9fc9"} 0.023276118 1719913187.631
```

That could be translated by:

* `easypay-service` handled an HTTP request,
* Which generated trace ID id `969873d828346bb616dca9547f0d9fc9`,
* Request duration was `0.023276118` second,
* At timestamp `1719913187.631`

👀 Exemplars can be analyzed in Grafana:

* Go to the Grafana `Explore` view,
* Select the `Prometheus` data source,
* Switch to the `Code` mode (button on the right),
* Paste the following PromQL query:

```
http_server_request_duration_seconds_bucket{http_route="/payments",service_name="easypay-service"}
```

* Unfold the `Options` section and enable `Exemplars`,
* Click on `Run query`.

👀 In addition to the line graph, you should see square dots at the bottom of the graph:

* Hover over a dot,
* It should display useful information for correlation, particularly a `trace_id`.

#### Enable correlation

🛠️ In Grafana, go to the `Connections` > `Data sources` section:

* Select the `Prometheus` data source,
* Click on `Exemplars`:
    * Enable `Internal link` and select the Tempo data source,
    * `URL Label`: `Go to Trace`,
    * `Label name:`: `trace_id` (as displayed in the exemplar values),
* Click on `Save & test`.

🛠️ Go back to the Grafana `Explore` dashboard and try to find the same exemplar as before:

* Hover over it,
* You should see a new button `Go to Trace` next to the `trace_id` label,
* Click on the button.

👀 Grafana should open a new pane with the corresponding trace from Tempo!

We have added a new correlation dimension to our system between metrics and traces!

#### How was it done?

Meters such as histograms are updated in the context of a request, and thus of a recorded trace.

When histogram is updated, the OpenTelemetry instrumentation library gets the current trace and span ids in the context, 
and adds them to the corresponding bucket _as an example_. 

Exemplars are then propagated with the metric to the time series database, which should support them.
Hopefully Prometheus can support them: we just had to start the server with the `--enable-feature=exemplar-storage` 
flag (see `compose.infrastructure.yml`) to enable their storage.