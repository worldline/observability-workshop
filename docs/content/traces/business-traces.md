+++
date = '2025-06-07T16:12:49+02:00'
title = 'Business Traces'
weight = 5
+++

Just like metrics, it is also possible to add your own spans on arbitrary methods to provide more business value to the
observability of your application.

Let’s return to our code!

## Objectives

We want to add new spans to the traces generated in the `easypay-service` application to track payment processing and
store events.

To achieve this goal, we will create new spans when the `process` and `store` methods of the
`com.worldline.easypay.payment.control.PaymentService` class in the `easypay-service` module are invoked.

As a reminder, this class is the central component responsible for processing payments. It provides the public method
`accept`, which delegates its responsibilities to two private methods:

* `process`: which handles all the processing of the payment, including validation and calling third parties.
* `store`: which saves the processing result in the database.

### 1. Add Required Dependencies

We need to add the `io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations` dependency to our module
to access some useful annotations.

👀 This has already been done in advance for this workshop. The following dependencies were added to the Gradle build
file (`build.gradle.kts`) of the `easypay-service` module:

```kotlin
dependencies {
    //...
    // Add opentelemetry Annotations support
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations")

    // ...
}
```

### 2. Add Custom Spans

📝 To add new spans based on methods, we can simply use the `@WithSpan` Java annotation. When a traced transaction
invokes the annotated method, a new span will be created. Here’s how to do it:

```java
// ...

import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class PaymentService {
    // ...

    @WithSpan("easypay: Payment processing method")
    private void process(PaymentProcessingContext context) {
        //...
    }

    @WithSpan("easypay: Payment store method")
    private void store(PaymentProcessingContext context) {
        //...
    }
    
    // ...
}
```

📝 We can also provide additional information to the span, such as method parameters using the ``@SpanAttribute``
annotation:

```java
// ...

import io.opentelemetry.instrumentation.annotations.SpanAttribute;

@Service
public class PaymentService {
    // ...

    @WithSpan("easypay: Payment processing method")
    private void process(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }

    @WithSpan("easypay: Payment store method")
    private void store(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }
    // ...
}
```

This will provide the whole PaymentProcessingContext into the trace.

### 3. Build and redeploy

🛠️ As we did before:

```bash
$ docker compose up -d --build easypay-service
```

### 4. Test it!

🛠️ Generate some payments:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
```

👀 Go back to Grafana and try to find your new traces using what you've learned previously. Observe the spans you added.

> [!CAUTION]
> It may take some time for `easypay-service` to be registered in the service discovery and be available from the API
> gateway.  
> Similarly, your traces being ingested by Tempo might also take some time. Patience is key 😅