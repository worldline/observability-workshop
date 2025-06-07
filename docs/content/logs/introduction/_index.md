+++
date = '2025-06-06T23:21:18+02:00'
title = 'Introduction'
weight = 1
+++

## Some functional issues

🛠️ It’s time to have a look to our `easypay-service` logs!  
Service is started in a Docker container. To get its output you can use the following command:

```bash
$ docker compose logs -f easypay-service
```

One of our customers raised an issue:

> « When I reach your API, I usually either an ``AMOUNT_EXCEEDED`` or ``INVALID_CARD_NUMBER`` error. »

Normally the first thing to do is checking the logs.
Before that, we will reproduce this behavior.

🛠️ You can check the API as following:

* For the ``AMOUNT_EXCEEDED`` error (any ``amount`` above 50000 would give the same result):

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=51000

HTTP/1.1 201 Created
Content-Type: application/json
Date: Wed, 05 Jun 2024 13:45:40 GMT
Location: http://172.19.25.95:44523/payments/5459b20a-ac91-458f-9578-019c05483bb3
transfer-encoding: chunked

{
    "amount": 51000,
    "authorId": "6ace318f-b669-4e4a-b366-3f09048becb7",
    "authorized": false,
    "bankCalled": true,
    "cardNumber": "5555567898780008",
    "cardType": "MASTERCARD",
    "expiryDate": "789456123",
    "paymentId": "5459b20a-ac91-458f-9578-019c05483bb3",
    "posId": "POS-01",
    "processingMode": "STANDARD",
    "responseCode": "AUTHORIZATION_DENIED",
    "responseTime": 25
}
```

* And for the ``INVALID_CARD_NUMBER`` error:

```bash
$  http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780007 expiryDate=789456123 amount:=25000

HTTP/1.1 201 Created
Content-Type: application/json
Date: Wed, 05 Jun 2024 13:46:09 GMT
Location: http://172.19.25.95:44523/payments/2dbf3823-fb11-4c63-a540-ab43ac663e68
transfer-encoding: chunked

{
    "amount": 51000,
    "authorId": null,
    "authorized": false,
    "bankCalled": false,
    "cardNumber": "5555567898780007",
    "cardType": null,
    "expiryDate": "789456123",
    "paymentId": "2dbf3823-fb11-4c63-a540-ab43ac663e68",
    "posId": "POS-01",
    "processingMode": "STANDARD",
    "responseCode": "INVALID_CARD_NUMBER",
    "responseTime": 5
}

```

👀 Look into the console `logs` to pinpoint these issues.

> [!INFO]
> As you can see, logs can give you quickly some information.
>
> They are easy to implement as most of our frameworks provides a logging mechanism (here logback, but could be log4j,
> log4j2, etc.). Unfortunately, they are also dependent of the work of the developer to be insightful, who can also miss
> some important information.
>
> If you want to dig into this particular topic, you can check out this
> article: [Back to basics - Logging](https://blog.worldline.tech/2020/01/22/back-to-basics-logging.html).

### Additional logs

It's time to add more contextual information into our code!

We will use in this workshop SLF4J. It is a logging facade that provides a simple API to log messages, and which can be
bind to different logging frameworks (Logback, Log4j, etc.).

The logger can be created by adding a static class variable such as:

```java
  private static final Logger log = LoggerFactory.getLogger(PaymentResource.class);
```

Think to use the corresponding class to instantiate it!

#### *What about log levels?*

Use the most appropriate log level

The log level is a fundamental concept in logging. Whether the logging framework you use, it allows you to tag log
records according to their severity or importance.
For instance, [SLF4J](https://www.slf4j.org/) offers
the [following log levels by default](https://www.slf4j.org/apidocs/org/slf4j/event/Level.html):

* ``TRACE`` : typically used to provide detailed diagnostic information that can be used for troubleshooting and
  debugging. Compare to DEBUG messages, TRACE messages are more fine-grained and verbose.
* ``DEBUG``: used to provide information that can be used to diagnose issues especially those related to program state.
* ``INFO``: used to record events that indicate that program is functioning normally.
* ``WARN``: used to record potential issues in your application. They may not be critical but should be investigated.
* ``ERROR``: records unexpected errors that occur during the operation of your application. In most cases, the error
  should be addressed as soon as possible to prevent further problems or outages.

We can also log payment requests and responses to provide even more context, which could be helpful for following
requests in this workshop.

#### *Add logs*

📝 Modify the `easypay-service/src/main/java/com/worldline/easypay/payment/boundary/PaymentResource.java` class by
uncommenting all `// LOG.…` lines (keep MDC lines for later 😉).

## The technical issue

Another issue was raised for the POS (Point of Sell) ``POS-02`` (but we didn’t know yet!).

🛠️ When you reach the API using this command:

```bash
http POST :8080/api/easypay/payments posId=POS-02 cardNumber=5555567898780008 expiryDate=789456123 amount:=25000
```

👀 You should get the following output:

````bash

HTTP/1.1 500
[...]

{
  "error":"Internal Server Error",
  "path": "/payments",
  "status": 500,
  [...]
}
````

👀 You then get the following log message:

```bash
2024-06-05T15:45:35.215+02:00 ERROR 135386 --- [easypay-service] [o-auto-1-exec-7] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.NullPointerException: Cannot invoke "java.lang.Boolean.booleanValue()" because "java.util.List.get(int).active" is null] with root cause

java.lang.NullPointerException: Cannot invoke "java.lang.Boolean.booleanValue()" because "java.util.List.get(int).active" is null
        at com.worldline.easypay.payment.control.PosValidator.isActive(PosValidator.java:34) ~[main/:na]
        at com.worldline.easypay.payment.control.PaymentService.process(PaymentService.java:46) ~[main/:na]
        at com.worldline.easypay.payment.control.PaymentService.accept(PaymentService.java:108) ~[main/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:354) ~[spring-aop-6.1.6.jar:6.1.6]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196) ~[spring-aop-6.1.6.jar:6.1.6]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163) ~[spring-aop-6.1.6.jar:6.1.6]
    [...]
```

### Let's fix it!

📝 To find the root cause, add first a _smart_ log entry in the
``easypay-service/src/main/java/com/worldline/easypay/payment/control/PosValidator.java`` class. In the ``isActive()``
method, catch the exception and trace the error:

```java
public boolean isActive(String posId) {
    PosRef probe = new PosRef();
    probe.posId = posId;
    try {
        List<PosRef> posList = posRefRepository.findAll(Example.of(probe));

        if (posList.isEmpty()) {
            LOG.warn("checkPosStatus NOK, unknown posId {}", posId);
            return false;
        }

        boolean result = posList.get(0).active;

        if (!result) {
            LOG.warn("checkPosStatus NOK, inactive posId {}", posId);
        }
        return result;
    } catch (NullPointerException e) {
        LOG.warn("Invalid value for this POS: {}", posId);
        throw e;
    }
}
```

🛠️ You can redeploy the `easypay-service` with the following commands:

```bash
$ docker compose up -d --build easypay-service
```

🛠️ Now you can run the same commands ran earlier and check again the logs (`http POST…`).

This is most likely an issue with some data in the database… Thanks to logs, we may quickly get the idea that the
problem comes from the point of sale with ID `POS-02`.

It is easy to identify the issue if we don’t have traffic against our application, but what if we have more realistic
traffic?

🛠️ Generate some load with `k6` (a Grafana tool for load testing):

```bash
$ k6 run -u 5 -d 5s k6/01-payment-only.js
```

👀 Check again logs:

```bash
$ docker compose logs -f easypay-service
```

Logs are now interleaved due to several requests being executed concurrently. It becomes harder to identify which point
of sale is causing this error...

👀 If you look into the SQL import file (`easypay-service/src/main/resources/db/postgresql/data.sql`), you'll notice a
`NULL` value instead of a boolean for the `active` column.

> [!CAUTION]
> Let’s keep the issue as it is for now.