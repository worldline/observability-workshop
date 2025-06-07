+++
date = '2025-06-06T23:24:44+02:00'
title = 'Mapped Diagnostic Context'
weight = 2
+++

Mapped Diagnostic Context (MDC) will help us add more context on every log output. For more information, refer to this
web page: [https://logback.qos.ch/manual/mdc.html](https://logback.qos.ch/manual/mdc.html).
It is a kind of Map attached to the Thread context and maintained by the logging framework where you can put values and
use them in your log layout.

As it is attached to a Thread, we can put values at the beginning of a request and print them in all the logs related to
this request.
That can give you a lot of information about the context of the request, without having to add them to all logs
manually.

A good sketch is better than a long speech. In the next section, we will use MDC to add the card number and the POS ID
to all the logs related to a request.

📝 Go to the ``easypay-service/src/main/java/com/worldline/easypay/payment/boundary/PaymentResource`` class and modify
the method ``processPayment()`` to instantiate the [MDC](https://logback.qos.ch/manual/mdc.html):

```java
public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
    // Add cardNumber to SLF4J MDC 
    MDC.put("cardNumber",paymentRequest.cardNumber());
    // Add Point Of Sale identifier to SLF4J MDC
    MDC.put("pos",paymentRequest.posId());
            
    try { // Add a try-finally construct and wrap the initial code here 
        //...
        return httpResponse;
    catch (Exception e) { 
        // Catch any exception to log it with MDC value
        LOG.error(e.getMessage());
        throw e;
    } finally {
        // Clear MDC at the end
        MDC.clear();
    }
}
```

> [!CAUTION]
> Don’t forget to clear the MDC at the end of the method to avoid any memory leak.

Now, we want to print these values when a log line is printed in the console.

📝 Modify to the spring configuration file (``easypay-service/src/main/resources/application.yaml``) and add the
`logging.level.pattern` property to add both the ``cardNumber`` & ``pos``fields to all logs:

```yaml
logging:
  pattern:
    level: "%5p [%mdc]"
```

> [!TIP]
> `%mdc` prints the full content of the MDC Map attached to the current thread.
>
> If you want to print a single value, you can use `%X{key}` where `key` is the key of the value you want to print.

```yaml
# Alternative to print specific fields instead
logging:
  pattern:
    level: "%5p [%X{cardNumber} - %X{pos}]"
```

> [!INFO]
> Using the Spring Boot ``logging.pattern.level`` property is just a way to configure the logback pattern. You can also
> use a logback configuration file to do the same thing.  
> You can also use the ``logging.pattern.correlation`` property (used by tracing) or a logback configuration file to do
> the same thing.

🛠️ Rebuild and redeploy the `easypay-service`:

```bash
$ docker compose up -d --build easypay-service
```

### Adding more content in our logs

🛠️ To have more logs, we will run several HTTP requests using [K6](https://k6.io/). Run the following command:

```bash
$ k6 run -u 5 -d 5s k6/01-payment-only.js
```

👀 Check then the logs to pinpoint some exceptions.