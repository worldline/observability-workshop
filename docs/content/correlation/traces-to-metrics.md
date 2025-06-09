+++
date = '2025-06-07T16:20:08+02:00'
title = 'Traces to Metrics'
weight = 5
+++

The last correlation we will explore in today’s workshop is between traces and metrics.

Sometimes, we are interested in knowing the state of our application when inspecting traces, such as JVM heap or system
CPU usage.

In this section, we will configure the Tempo data source to link our traces to these metrics.

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Tempo` data source,
* Configure `Trace to metrics` as follows:
    * Data source: `Prometheus`,
    * Span start time shift: `-2m`,
    * Span end time shift: `2m`,
    * Tags: `service.name` as `service_name`

🛠️ Now, we will add some metric queries (click on `+ Add query` for each query):

* Heap usage as ratio:
    * Link Label: `Heap Usage (ratio)`,
    * Query: `sum(jvm_memory_used_bytes{$__tags})/sum(jvm_memory_limit_bytes{$__tags})`
* System CPU usage:
    * Link Label: `System CPU Usage`,
    * Query: `jvm_cpu_recent_utilization_ratio{$__tags}`

> [!INFO]
> `$__tags` will be expanded by the tags defined in the `Tags` section.  
> For `easypay-service`, the query becomes `jvm_cpu_recent_utilization_ratio{application=easypay-service}`

🛠️ Finally click on `Save & test` and go back to Grafana `Explore` dashboard to test our new setup.

#### Test correlation

🛠️ Hit the easypay payment endpoint with curl or k6 to generate some traces (whichever you prefer):

* `http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000`
* `k6 run -u 1 -d 2m k6/01-payment-only.js`

🛠️ Open the Grafana `Explore` dashboard to find a trace:

* You can use the following TraceQL directly: `{name="POST easypay-service"}`
    * It is equivalent to filtering on Span Name: `POST easypay-service`
    * More information about TraceQL: [Grafana TraceQL documentation](https://grafana.com/docs/tempo/latest/traceql/)
* Drill down a trace (you can widen the new pane).

👀 The previous **LOG** icon has been replaced by a link icon (middle of the screen):

* Click on the icon for several spans (for the various services involved in the transaction),
    * You have now three choices: `Heap Usage (ratio)`, `System CPU Usage` and `Related logs`,
    * `Related logs` behaves the same way as the previous **LOG** button.

This was the last correlation dimension we wanted to show you!