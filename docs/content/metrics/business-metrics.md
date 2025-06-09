+++
date = '2025-06-07T15:59:02+02:00'
title = 'Business Metrics'
weight = 4
+++

Observability is not only about incidents. You can also define your own metrics.

[OpenTelemetry API](https://opentelemetry.io/docs/languages/java/) provides an API to create your

* [Counters](https://opentelemetry.io/docs/languages/java/api/#counter): value which can only increment (
  such as the number of processed requests),
* [Gauges](https://opentelemetry.io/docs/languages/java/api/#gauge): represents the current value (such as
  the speed gauge of a car),
* [Histograms](https://opentelemetry.io/docs/languages/java/api/#histogram): to record values with large distribution
  such as latencies.

Let’s go back to our code!

## Objectives

We want to add new metrics to the easypay service to measure they payment processing and store time.  
So we target a metric of **Histogram** type.

In order to achieve this goal, we will measure the time spent in the two methods `process` and `store` of the
`com.worldline.easypay.payment.control.PaymentService` class of the `easypay-service` module.  
This class is the central component responsible for processing payments: it provides the ``accept`` public method, which
delegates its responsibility to two private ones:

* ``process``: which does all the processing of the payment: validation, calling third parties…
* ``store``: to save the processing result in database.

We also want to count the number of payment requests processed by our system. We will use a metric of **Counter** type.

## 1. Add the opentelemetry-api dependency

We need to add the `opentelemetry-api` dependency to `easypay-service` in order to use the OpenTelemetry API to create
custom metrics.

📝 Add the following dependency to the `easypay-service` `build.gradle.kts` file:

```kotlin
dependencies {
    // ...
    implementation("io.opentelemetry:opentelemetry-api")
}
```

## 2. Declare the histogram

We need to declare two timers in our code:

* ``processTimer`` to record the ``easypay.payment.process`` metric: it represents the payment processing time and
  record the time spent in the `process` method,
* ``storeTimer`` to record the ``easypay.payment.store`` metric: it represents the time required to store a payment
  in database by recording the time spent in the `store` method.

📝 Let’s modify the ``com.worldline.easypay.payment.control.PaymentService`` class to declare them:

```java
// ...

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.GlobalOpenTelemetry;

@Service
public class PaymentService {
    // ...
    private LongHistogram processHistogram;  // (1)
    private LongHistogram storeHistogram;

    public PaymentService(/* ... */) {
        // ...

        OpenTelemetry openTelemetry = GlobalOpenTelemetry.get(); // (2)

        processHistogram = openTelemetry.getMeter(EasypayServiceApplication.class.getName())  //(3)
                .histogramBuilder("easypay.payment.process")  // (4)
                .setDescription("Payment processing time") // (5)
                .setUnit("ms") // (6)
                .ofLongs() // (7)
                .build();
        storeHistogram = openTelemetry.getMeter(EasypayServiceApplication.class.getName())
                .histogramBuilder("easypay.payment.store")
                .setDescription("Payment storing time")
                .setUnit("ms")
                .ofLongs()
                .build();
    }
}
```

1. Declare the two timers,
2. Inject the OpenTelemetry instance to get the Meter object to create the histograms,
3. Initialize the two histograms by giving them a name (4), a description (5), a unit (6) and setting the type of the
   values (7).

## 3. Record time spent in the methods

📝 Let’s modify our `process` and `store` methods to record our latency with the new metrics.  
We can simply wrap our original code in a `try-finally` construct such as:

```java
    // ...
private void process(PaymentProcessingContext context) {
    long startTime = System.currentTimeMillis(); // (1)
    try { // (2)
        if (!posValidator.isActive(context.posId)) {
            context.responseCode = PaymentResponseCode.INACTIVE_POS;
            return;
        }
        // ...
    } finally {
        long duration = System.currentTimeMillis() - startTime; // (3)
        processHistogram.record(duration); // (4)
    }
}

private void store(PaymentProcessingContext context) {
    long startTime = System.currentTimeMillis(); // (5)
    try {
        Payment payment = new Payment();
        // ...
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        storeHistogram.record(duration);
    }
}
```

1. Get the start time,
2. Wrap the original code in a `try-finally` construct,
3. Compute the duration,
4. Record the duration in the `processHistogram` histogram,
5. Do the same for the `store` method.

#### 4. Add counter

📝 Let’s do the same for the counter:

```java
// ...

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;


@Service
public class PaymentService {
    //...
    private LongCounter requestCounter; // (1)

    public PaymentService(/* ... */) {
        // ...
        requestCounter = openTelemetry.getMeter(EasypayServiceApplication.class.getName()) // (2)
                .counterBuilder("easypay.payment.requests")
                .setDescription("Payment requests counter")
                .build();
    }
}
```

1. Declares the counter,
2. Initializes the counter.

📝 The method ``accept`` of the ``PaymentService`` class is invoked for each payment request, it is a good candidate to
increment our counter:

```java

@Transactional(Transactional.TxType.REQUIRED)
public void accept(PaymentProcessingContext paymentContext) {
    requestCounter.add(1); // < Add this (1)
    process(paymentContext);
    store(paymentContext);
    paymentTracker.track(paymentContext);
}
```

1. Increment the counter each time the method is invoked.

## 5. Redeploy easypay

🛠️ Rebuild and redeploy `easypay-service`:

```bash
$ docker compose up -d --build easypay-service
```

🛠️ Once easypay is started (you can check logs with the ``docker compose logs -f easypay-service`` command and wait for
an output like ``Started EasypayServiceApplication in 32.271 seconds``):

* Execute some queries:

```bash
$ k6 run -u 1 -d 1m k6/01-payment-only.js
```

🛠️ Then go to Grafana and explore Metrics to find your newly created metrics:

* Search for metric with base name `easypay_payment_process`,
* 👀 You should get 3 new metrics:
    * `easypay_payment_process_milliseconds_bucket`,
    * `easypay_payment_process_milliseconds_count`,
    * `easypay_payment_process_milliseconds_sum`.

👀 Explore them, especially the `_bucket` one.

When using a `Histogram` you get several metrics by default, suffixed with:

* `_bucket`: contains the number of event which lasts less than the value defined in the `le` tag,
* `_count`: the number of hits,
* `_sum`: the sum of time spent in the method.

Especially:

* We can get the average time spent in the method by dividing the `sum` by the `count`,
* We can calculate the latency percentile thanks to the buckets.

Finally, our ``Counter`` becomes a metric suffixed with ``_total``: `easypay_payment_requests_total`.

## 6. Compute percentiles

Let’s compute percentiles for the `process` and `store` methods.

As we have seen, the `Histogram` metric provides the necessary data to compute percentiles, we can
query Prometheus to display the percentiles of our application:

🛠️ Go to Grafana, to explore Metrics again.

🛠️ To compute the percentiles for the `easypay_payment_process` histogram we have created:

* Select the `easypay_payment_process_milliseconds_bucket` metric,
* Click on `Operations` and select `Aggregations` > `Histogram quantile`,
* Select a Quantile value,
* Click on `Run query`.

## 7. Visualization

🛠️ Go back to Grafana (`port 3000`), and go into the ``Dashboards`` section.

🛠️ We will import the dashboard defined in the ``docker/grafana/dashboards/easypay-monitoring.json`` file:

* Click on ``New`` (top right), and select ``Import``,
* In the ``Import via dashboard JSON model`` field, paste the content of the ``easypay-monitoring.json``  file and click
  on ``Load``,
* Select Prometheus as a data source.

You should be redirected to the ``Easypay Monitoring`` dashboard.

It provides some dashboards we have created from the new metrics you exposed in your application:

* `Payment request count total (rated)`: represents the number of hit per second in our application computed from our
  counter,
* ``Payment Duration distribution``: represents the various percentiles of our application computed from the
  ``easypay_payment_process`` histogram,
* ``Requests process performance`` and ``Requests store performance``: are a visualization of the buckets of the two
  histograms we created previously.

🛠️ You can generate some load to view your dashboards evolving live:

```bash
$ k6 run -u 2 -d 2m k6/01-payment-only.js
```

> [!NOTE]
> Do not hesitate to explore the way the panels are created, and the queries we used!  
> Just hover the panel you are interested in, click on the three dots and select Edit.