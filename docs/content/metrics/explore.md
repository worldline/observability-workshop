+++
date = '2025-06-07T15:56:26+02:00'
title = 'Explore'
weight = 3
+++

## Let's explore the metrics

> [!INFO]
> For this workshop, we have already configured in Grafana the Prometheus datasource.  
> You can have a look at its configuration in Grafana (``port 3000``) in the ``Connections`` > ``Data sources``
> section.  
> It is pretty straightforward as we have only set the Prometheus server URL.

🛠️ Go to Grafana and start again an ``Explore`` dashboard.

🛠️ Select the ``Prometheus`` datasource instead of the ``Loki`` one.

In this section you will hand on the metrics query builder of Grafana.

The ``Metric`` field lists all the metrics available in the Prometheus server: take time to explore them.

🛠️ For example, you can select the metric named ``jvm_memory_used_bytes``, and click on the ``Run query`` button to plot
the memory usage of all your services by memory area,

🛠️ If you want to plot the total memory usage of your services:

* Click on ``Operations`` and select ``Aggregations`` > ``Sum``, and ``Run query``: you obtain the whole memory
  consumption of all your JVMs,
* To split the memory usage per service, you can click on the ``By label`` button and select the label named
  ``service-name`` (do not forget to click on ``Run query`` afterthat).

🛠️ You can also filter metrics to be displayed using ``Label filters``: try to create a filter to display only the
metric related to the service named easypay-service.

> [!TIP]
> At the bottom of the query builder, you should see something like:  
> `sum by(service_name) (jvm_memory_used_bytes{service_name="easypay-service"})`.  
> This is the effective query raised by Grafana to Prometheus in order to get its metrics.  
> This query language is named [PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/).

### Dashboards

With Grafana, you can either create your own dashboards or import some provided by the community from 
[Grafana’s dashboards collection](https://grafana.com/grafana/dashboards/).

We will choose the second solution right now and import the following dashboards:

* [JMX Overview (Opentelemetry)](https://grafana.com/grafana/dashboards/12271-jvm-micrometer/), which ID is `17582`,
* [OpenTelemetry JDBC Dashboard](https://grafana.com/grafana/dashboards/20729-spring-boot-jdbc-hikaricp/), which ID is
  `19732`.

🛠️ To import these dashboards:

* Go to Grafana (``port 3000``), and select the ``Dashboards`` section on the left,
* Then click on ``New`` (top right), and click on ``Import``,
* In the ``Find and import…`` field, just paste the ID of the dashboard and click on ``Load``,
* In the ``Select a Prometheus data source``, select ``Prometheus`` and click on ``Import``,
* You should be redirected to the newly imported dashboard.

> [!TIP]
> Imported dashboards are available directly from the ``Dashboards`` section of Grafana.

👀 Explore the ``JMX Overview`` dashboard: it works almost out of box.  
It contains a lot of useful information about JVMs running our services.

The ``job`` filter (top of the dashboard) let you select the service you want to explore metrics.

### Incident!

🛠️ Now let's simulate some traffic using [Grafana K6](https://k6.io/). Run the following command:

```bash
$ k6 run -u 5 -d 2m k6/02-payment-smartbank.js
```

👀 Go back to the Grafana dashboard, click on ``Dashboards`` and select ``JVM Micrometer``:

* Explore the dashboard for the ``easypay-service``, especially the Garbage collector and CPU statistics.

* Look around the other ``OpenTelemetry JDBC`` dashboard then and see what happens on the database connection
  pool for ``easypay-service``.

We were talking about an incident, isn’t it?

👀 Let's go back to the Explore view of Grafana, select Loki as a data source and see what happens!

🛠️ Create a query with the following parameters to get error logs of the ``smartbank-gateway`` service:

* Label filters: ``service_name`` = ``smartbank-gateway``
* label filter expression: ``label`` = ``detected_level ; ``operator`` = ``=~`` ; ``value`` = ``warn|error``

🛠️ Click on ``Run query`` and check out the logs.

Normally you would get a ``java.lang.OutOfMemoryError`` due to a saturated Java heap space.

👀 To get additional insights, you can go back to the `JMX Overview` dashboard and select the ``smartbank-gateway`` application.

Normally you will see the used JVM Heap reaching the maximum allowed.

> [!INFO]
> Grafana and Prometheus allows you to generate alerts based on metrics,
> using [Grafana Alertmanager](https://grafana.com/docs/grafana/latest/alerting/set-up/configure-alertmanager/).  
> For instance, if CPU usage is greater than 80%, free memory is less than 1GB, used heap is greater than 80%, etc.

🛠️ You may have to restart smartbank:

```bash
$ docker compose restart smartbank-gateway
```