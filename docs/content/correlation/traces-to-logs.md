+++
date = '2025-06-07T16:19:18+02:00'
title = 'Traces to Logs'
weight = 4
+++

We are able to link logs to traces thanks to Loki’s data source `Derived fields`, but what about traces to logs?

Fortunately, the Tempo data source can be configured the same way!

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Tempo` data source,
* Configure `Trace to logs` as follows:
    * Data source: `Loki`,
    * Span start time shift: `-5m`,
    * Span end time shift: `5m`,
    * Tags: `service.name` as `service_name`,
    * Enable `Filter by trace ID` if and only if you want to show logs that match the trace ID.
* Click on `Save & test`.

ℹ️ Just to give you some context about this configuration:

* *Span start time shift* and *Span end time shift* allow retrieving logs within the specified interval, as log
  timestamps and trace timestamps may not match exactly.
* *Tags* is required: we should have a common tag between Tempo and Loki to bridge the gap between traces and logs.
  Here, the application name (defined as `service.name` in Tempo and `service_name` in Loki) serves this purpose.
* Using filters will remove logs that do not match trace or span identifiers.

#### Test correlation

🛠️ Hit the easypay payment endpoint with curl or k6 to generate some traces (whichever you prefer):

* `http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000`
* `k6 run -u 1 -d 2m k6/01-payment-only.js`

🛠️ Open the Grafana `Explore` dashboard to find a trace:

* You can use the following TraceQL directly: `{name="POST easypay-service"}`
    * It is equivalent to filtering on Span Name: `POST easypay-service`
    * More information about TraceQL: [Grafana TraceQL documentation](https://grafana.com/docs/tempo/latest/traceql/)
* Drill down a trace (you can widen the new pane).

👀 A new **LOG** icon should appear for each line of the trace (middle of the screen):

* Click on the icon for several spans (for the various services involved in the transaction),
* You can try toggling the `Filter by trace ID` option in the Tempo data source configuration to see the difference.

Yeah, we have added a new dimension to the correlation of our telemetry data, further improving our observability.