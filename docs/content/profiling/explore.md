+++
date = '2025-06-07T16:25:15+02:00'
title = 'Explore'
weight = 3
+++

> [!INFO]
> We already configured the Pyroscope data source in Grafana.  
> You can take a look at its configuration in the `Connections` > `Data sources` section.

👀 Let’s go to the Grafana `Explore` dashboard:

* Select the `Pyroscope` data source,
* For the profiling type, select `process_cpu` > `cpu`,
* In the field next to the profiling type, enter a filter to get profiling of the `easypay-service`:
  `{service_name="easypay-service"}`,
* Select another profiling type (such as memory allocation in TLAB).

🛠️ Generate some load with `k6`:

```bash
$ k6 run -u 1 -d 5m k6/02-payment-smartbank.js
```