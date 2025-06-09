+++
date = '2025-06-07T16:06:26+02:00'
title = 'Explore'
weight = 3
+++

## Explore Traces with Grafana

> [!INFO]
> For this workshop, we've already configured the Tempo datasource in Grafana.
> You can take a look at its configuration in Grafana (available on port ``3000``) by navigating to the `Connections` >
`Data sources` section.
> Similar to Prometheus, the configuration is quite straightforward as we only need to set up the Tempo server URL.

🛠️ Generate some load on the application to produce traces:

```bash
$ k6 run -u 1 -d 5m k6/01-payment-only.js
```

🛠️ Let’s explore your first traces in Grafana:

* Go to Grafana and open an ``Explore`` dashboard,
* Select the `Tempo` data source and click on ``Run query`` to refresh the view.

> [!CAUTION]
> You may need to wait one or two minutes to allow Tempo to ingest some traces…

👀 Click on `Service Graph` and explore the `Node graph`: this view is extremely helpful for visualizing and
understanding how our services communicate with each other.

👀 Go back to `Search` and click on `Run query`. You should see a table named `Table - Traces`.
By default, this view provides the most recent traces available in *Tempo*.

🛠️ Let's find an interesting trace using the query builder:

* Look at all traces corresponding to a POST to `easypay-service` with a duration greater than 50 ms:
    * Span Name: `POST easypay-service`
    * Duration: `trace` `>` `50ms`
    * You can review the generated query, which uses a syntax called TraceQL.
* Click on `Run query`.
* Sort the table by `Duration` (click on the column name) to find the slowest trace.
* Drill down a `Trace ID`.

You should see the full stack of the corresponding transaction.

👀 Grafana should open a new view (you can enlarge it by clicking on the three vertical dots and selecting `Widen pane`):

* Pinpoint the different nodes and their corresponding response times:
    * Each line is a span and corresponds to the time spent in a method/event.
* Examine the SQL queries and their response times.
* Discover that distributed tracing can link transactions through:
    * HTTP (`api-gateway` to `easypay-service` and `easypay-service` to `smartbank-gateway`).
    * Kafka (`easypay-service` to `fraudetect-service` and `merchant-backoffice`).

🛠️ Grafana allows to display a graph of spans as interconnected nodes:

* Modify the Tempo data source:
    * Go to `Additional settings`,
    * Check the `Enable node graph` option.
* Go back to the same kind of trace,
* Click on `Node graph` to get a graphical view of all the spans participating in the trace.

🛠️ Continue your exploration in the `Search` pane:

* For example, you can add the `Status` `=` `error` filter to see only traces that contain errors,
* Try to find our requests with a `NullPointerException`.
