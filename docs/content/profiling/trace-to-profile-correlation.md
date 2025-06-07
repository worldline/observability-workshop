+++
date = '2025-06-07T16:26:14+02:00'
title = 'Trace to Profile Correlation'
weight = 4
+++

It is possible to link traces to profiles in Grafana Pyroscope, thanks to the Pyroscope extension for the OpenTelemetry Agent.
This extensions attaches span context to profiles making possible to correlate traces with profiles.

#### Configure correlation

️🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Tempo` data source,
* Configure `Trace to profiles` as follows:
    * Data source: `Pyroscope`,
    * Tags: `service.name` as `application`,
    * Profile type: `process_cpu` > `cpu`.

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

👀 In the link icon, at the root of a service, select `Related Profile`.