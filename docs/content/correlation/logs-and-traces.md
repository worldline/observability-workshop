+++
date = '2025-06-07T16:16:10+02:00'
title = 'Logs and Traces'
weight = 2
+++

🛠️ Let's go back to the Grafana `Explore` dashboard:

* Select the ``Loki`` data source,
* Add a label filter to select logs coming from ``easypay-service``,
* Run a query and select a log entry corresponding to a payment query.

👀 Now check to see if there are
``trace_id``](https://www.w3.org/TR/trace-context/#trace-id) and [
``span_id``](https://www.w3.org/TR/trace-context/#parent-id) attributes.
These will help us correlate our different request logs and traces.

> [!INFO]
>
> These concepts are part of the [W3C Trace Context Specification](https://www.w3.org/TR/trace-context/).

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Loki` data source,
* Create a `Derived fields` configuration:
    * `Name`: `TraceID`
    * `Type`: `Label`
    * `Label`: `trace_id`
    * `Query`: `${__value.raw}`
    * `URL Label`: `View Trace`
    * Enable `Internal Link` and select `Tempo`.


🛠️ Go back to the Grafana `Explore` dashboard and try to find the same kind of log message:

* Expand the log,
* At the bottom of the log entry, you should find the `Fields` and `Links` sections,
* If the log contains a trace ID, you should see a button labeled `View Trace`,
* Click on this button!

👀 Grafana should open a pane with the corresponding trace from Tempo!

Now you can correlate logs and traces!  
If you encounter any exceptions in your error logs, you can now see where it happens and get the bigger picture of the
transaction from the customer's point of view.

#### How was it done?

First of all, logs should contain the `trace_id` information.  
Most frameworks handle this for you. Whenever a request generates a trace or span, the value is placed in the MDC (
Mapped Diagnostic Context) and can be printed in the logs.

On the other hand, Grafana has the ability to parse logs to extract certain values for the Loki data source. This is the
purpose of `Derived fields`.

When configuring the Loki data source, we provided Grafana with the trace ID label to use from logs and linked
it to the Tempo data source. Behind the scenes, Grafana creates the bridge between the two telemetry data sources. And
that’s all 😎