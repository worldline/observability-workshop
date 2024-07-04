authors: David Pequegnot & Alexandre Touret
summary: Let your Java application be truly observable! Let's dive into logs, traces and monitoring with the Grafana Stack
id: observability-workshop
categories: observability, java
environments: Web
status: Published
feedback link: https://github.com/worldline/observability-workshop/issues

# Make your Java application fully observable with the Grafana Stack

## Introduction

This workshop aims to introduce how to make a Java application fully observable with:
* Logs with insightful information
* Metrics with [Prometheus](https://prometheus.io/)
* [Distributed Tracing](https://blog.touret.info/2023/09/05/distributed-tracing-opentelemetry-camel-artemis/)

During this workshop we will use the Grafana stack and Prometheus:

* [Grafana](https://grafana.com/): for dashboards
* [Loki](https://grafana.com/oss/loki/): for storing our logs
* [Tempo](https://grafana.com/oss/tempo/): for storing traces
* [Prometheus](https://prometheus.io/): for gathering and storing metrics.

We will also cover the OpenTelemetry Collector which gathers & broadcasts then the data coming from our microservices

## Workshop overview

### Application High Level Design

![The Easy Pay System](./img/architecture.svg)

#### API Gateway
Centralises all API calls.

#### Easy Pay Service
Payment microservices which accepts (or not) payments.

This is how it validates every payment:
1. Check the POS (Point of Sell) number
2. Check the credit card number
3. Check the credit card type
4. Check the payment threshold, it calls the Smart Bank Gateway for authorization

If the payment is validated, it stores it and broadcasts it to all the other microservices through Kafka.

#### Fraud detection Service

After fetching a message from the Kafka topic, this service search in its database if the payment's card number is registered for fraud.

In this case, only a ``WARN`` log is thrown.

#### Merchant Back Office Service
For this lab, it only simulates the subscription of messages.

#### Smart Bank Gateway
This external service authorizes the payment.

### Our fully observable platform
![The Easy Pay observable System](./img/architecture_observable.png)

#### Short explanation

As mentioned earlier, our observability stack is composed of :
* [Prometheus](https://prometheus.io/) for gathering & storing the metrics
* [Loki](https://grafana.com/oss/loki/) for storing the logs
* [Tempo](https://grafana.com/oss/tempo/) for storing the traces
* [Grafana](https://grafana.com/) for the dashboards
*  [Grafana Alloy - OTEL collector](https://grafana.com/docs/alloy/latest/) which gathers all the data to send it then to 

In addition, the microservices are started with an agent to broadcast the traces to the collector.   

### Icons

You will the following icons during the workshop:

🛠️ An action to perform,
📝 A file to modify,
👀 Something to observe,
✅ Validate something,
ℹ️ Some information.

## Prerequisites
### Skills

| Skill                                                                                                                                                                                                                                                                                   | Level      | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| [REST API](https://google.aip.dev/general)                                                                                                                                                                                                                                              | proficient |
| [Java](https://www.oracle.com/java/)                                                                                                                                                                                                                                                    | proficient |   
| [Gradle](https://gradle.org/)                                                                                                                                                                                                                                                           | novice     |
| [Spring Framework](https://spring.io/projects/spring-framework), [Boot](https://spring.io/projects/spring-boot), [Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_quick_start), [Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) | proficient |
| [Docker](https://docs.docker.com/)                                                                                                                                                                                                                                                      | novice     |
| [Grafana stack](https://grafana.com/)                                                                                                                                                                                                                                                   | novice     |
| [Prometheus](https://prometheus.io/)                                                                                                                                                                                                                                                    | novice     |
| [Kafka](https://kafka.apache.org/)                                                                                                                                                                                                                                                      | novice     |

### Tools
#### If you want to execute this workshop locally
You **MUST** have set up these tools first:
* [Java 21+](https://adoptium.net/temurin/releases/?version=21)
* [Gradle 8.7+](https://gradle.org/)
* [Docker](https://docs.docker.com/) & [Docker compose](https://docs.docker.com/compose/)
* Any IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea), [VSCode](https://code.visualstudio.com/), [Netbeans](https://netbeans.apache.org/),...) you want
* [cURL](https://curl.se/), [jq](https://stedolan.github.io/jq/), [HTTPie](https://httpie.io/) or any tool to call your REST APIs

🛠️ Here are commands to validate your environment:

**Java**

```jshelllanguage
$ java -version

openjdk version "21.0.3" 2024-04-16 LTS
OpenJDK Runtime Environment Temurin-21.0.3+9 (build 21.0.3+9-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.3+9 (build 21.0.3+9-LTS, mixed mode, sharing)
```

**Gradle**

🛠️ If you use the wrapper, you won't have troubles. Otherwise...:

```jshelllanguage
$ gradle -version

------------------------------------------------------------
Gradle 8.7
------------------------------------------------------------

Build time:   2024-03-22 15:52:46 UTC
Revision:     650af14d7653aa949fce5e886e685efc9cf97c10

Kotlin:       1.9.22
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          21.0.3 (Eclipse Adoptium 21.0.3+9-LTS)
OS:           Linux 5.15.146.1-microsoft-standard-WSL2 amd64
```

**Docker Compose**

``` bash
$ docker compose version
    
Docker Compose version v2.24.7
```

#### If you don't want to bother with a local setup
It's strongly recommended to use [Gitpod](https://gitpod.io).
You must create an account first.
You then can open this project in either your local VS Code or directly in your browser:

[![Open in Gitpod](img/open-in-gitpod.svg)](https://gitpod.io/#github.com/worldline/observability-workshop.git)

## Environment Setup

### Open GitPod

We will assume you will use GitPod for this workshop :) 

[![Open in Gitpod](img/open-in-gitpod.svg)](https://gitpod.io/#github.com/worldline/observability-workshop.git)

When a messages invites you making an URL public, select and validate it.

### Start the infrastructure

The "infrastructure stack" is composed of the following components:
* One [PostgreSQL](https://www.postgresql.org/) instance per micro service
* One [Kafka broker](https://kafka.apache.org/)
* One [Service Discovery](https://spring.io/guides/gs/service-registration-and-discovery) microservice to enable load balancing & loose coupling.
* One [Configuration server](https://docs.spring.io/spring-cloud-config/) is also used to centralise the configuration of our microservices.
* The following microservices: API Gateway, Merchant BO, Fraud Detect, Smart Bank Gateway

ℹ️ If you run your application on GitPod, the following step are automatically started during the provisioning of your GitPod environment.

🛠️ Otherwise, to run it on your desktop, execute the following commands

``` bash
$ bash scripts/download-agent.sh
```

``` bash
$ ./gradlew tasks
```

``` bash
$ docker compose up -d --build --remove-orphans
```

✅ To check if all the services are up, you can run this command:

``` bash
$ docker compose ps -a
```
And check the status of every service.

For instance:

```bash
❯ docker compose ps
NAME                                           IMAGE                         COMMAND                  SERVICE               CREATED         STATUS                     PORTS
api-gateway                                    api-gateway:latest            "java -javaagent:/ap…"   api-gateway           8 minutes ago   Up 7 minutes (healthy)     0.0.0.0:8080->8080/tcp, :::8080->8080/tcp
config-server                                  config-server:latest          "java -javaagent:/ap…"   config-server         8 minutes ago   Up 7 minutes (healthy)     0.0.0.0:8888->8888/tcp, :::8888->8888/tcp
discovery-server                               discovery-server:latest       "java -javaagent:/ap…"   discovery-server      8 minutes ago   Up 7 minutes (healthy)     0.0.0.0:8761->8761/tcp, :::8761->8761/tcp
easypay-service                                easypay-service:latest        "java -javaagent:/ap…"   easypay-service       8 minutes ago   Up 7 minutes (healthy)     
fraudetect                                     fraudetect-service:latest     "java -javaagent:/ap…"   fraudetect-service    8 minutes ago   Up 7 minutes (healthy)     
kafka                                          confluentinc/cp-kafka:7.6.1   "/etc/confluent/dock…"   kafka                 8 minutes ago   Up 8 minutes (healthy)     9092/tcp, 0.0.0.0:19092->19092/tcp, :::19092->19092/tcp
merchant-backoffice                            merchant-backoffice:latest    "java -javaagent:/ap…"   merchant-backoffice   8 minutes ago   Up 7 minutes (healthy)     
observability-workshop-collector-1             grafana/alloy:latest          "/bin/alloy run --se…"   collector             8 minutes ago   Up 8 minutes               0.0.0.0:4317-4318->4317-4318/tcp, :::4317-4318->4317-4318/tcp, 0.0.0.0:12345->12345/tcp, :::12345->12345/tcp
observability-workshop-grafana-1               grafana/grafana:latest        "sh -xeuc 'mkdir -p …"   grafana               8 minutes ago   Up 7 minutes               0.0.0.0:3000->3000/tcp, :::3000->3000/tcp
observability-workshop-loki-1                  grafana/loki:latest           "/usr/bin/loki -conf…"   loki                  8 minutes ago   Up 7 minutes               0.0.0.0:3100->3100/tcp, :::3100->3100/tcp
observability-workshop-postgres-easypay-1      postgres:16                   "docker-entrypoint.s…"   postgres-easypay      8 minutes ago   Up 8 minutes (healthy)     0.0.0.0:5432->5432/tcp, :::5432->5432/tcp
observability-workshop-postgres-fraudetect-1   postgres:16                   "docker-entrypoint.s…"   postgres-fraudetect   8 minutes ago   Up 7 minutes (healthy)     0.0.0.0:5434->5432/tcp, :::5434->5432/tcp
observability-workshop-postgres-merchantbo-1   postgres:16                   "docker-entrypoint.s…"   postgres-merchantbo   8 minutes ago   Up 8 minutes (healthy)     0.0.0.0:5435->5432/tcp, :::5435->5432/tcp
observability-workshop-postgres-smartbank-1    postgres:16                   "docker-entrypoint.s…"   postgres-smartbank    8 minutes ago   Up 7 minutes (healthy)     0.0.0.0:5433->5432/tcp, :::5433->5432/tcp
observability-workshop-prometheus-1            prom/prometheus:v2.52.0       "/bin/prometheus --c…"   prometheus            8 minutes ago   Up 8 minutes               0.0.0.0:9090->9090/tcp, :::9090->9090/tcp
observability-workshop-tempo-1                 grafana/tempo:latest          "/tempo -config.file…"   tempo                 8 minutes ago   Up 7 minutes               0.0.0.0:3200->3200/tcp, :::3200->3200/tcp, 0.0.0.0:9095->9095/tcp, :::9095->9095/tcp, 0.0.0.0:9411->9411/tcp, :::9411->9411/tcp, 0.0.0.0:14268->14268/tcp, :::14268->14268/tcp
smartbank-gateway                              smartbank-gateway:latest      "java -Xmx4g -javaag…"   smartbank-gateway     8 minutes ago   Up 7 minutes (unhealthy)   

```

#### Validation

✅ Open the [Eureka](https://cloud.spring.io/spring-cloud-netflix/) website started during the infrastructure setup. The following instances should be registered with Eureka:

* API-GATEWAY
* EASYPAY-SERVICE
* FRAUDETECT-SERVICE
* MERCHANT-BACKOFFICE
* SMARTBANK-GATEWAY

> aside positive
>
> If you run this workshop on your desktop, you can go to this URL: [http://localhost:8761].  
> If you run it on GitPod, you can go to the corresponding URL (e.g., https://8761-worldline-observability-w98vrd59k5h.ws-eu114.gitpod.io) instead by going into the `PORTS` view.

🛠️ You can now access our platform to initiate a payment:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=25000
```

✅ You should get the following content:

```bash
HTTP/1.1 201 Created
Content-Type: application/json
Date: Wed, 05 Jun 2024 13:42:12 GMT
Location: http://172.19.25.95:44523/payments/3cd8df14-8c39-460b-a429-dc113d003aed
transfer-encoding: chunked

{
    "amount": 25000,
    "authorId": "5d364f1a-569c-4c1d-9735-619947ccbea6",
    "authorized": true,
    "bankCalled": true,
    "cardNumber": "5555567898780008",
    "cardType": "MASTERCARD",
    "expiryDate": "789456123",
    "paymentId": "3cd8df14-8c39-460b-a429-dc113d003aed",
    "posId": "POS-01",
    "processingMode": "STANDARD",
    "responseCode": "ACCEPTED",
    "responseTime": 414
}
```

## Logs 

### Some functional issues
One of our customers raised an issue: 

> When I reach your API, I usually either an ``AMOUNT_EXCEEDED`` or ``INVALID_CARD_NUMBER`` error.

Normally the first thing to do is checking the logs. 
Before that, we will reproduce these issues.

🛠️ You can check the API as following:

* For the ``AMOUNT_EXCEEDED`` error:

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
$  http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780007 expiryDate=789456123 amount:=51000

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

🛠️ It’s time to have a look to our `easypay-service` logs!  
Service is started in a Docker container. To get its output you can use the following command:

```bash
$ docker compose logs -f easypay-service
```

👀 Look into the console `logs` to pinpoint these issues.

> aside positive
>
> As you can see, the logs are not helpful for getting more information such as the business or user context.
> 
> If you want to dig into this particular topic, you can check out [this article](https://blog.worldline.tech/2020/01/22/back-to-basics-logging.html).

### Let's fix it!

It's time to add more contextual information into our code!

We will use in this workshop SLF4J.

The logger can be created by adding a class variable such as:

```java
  private static final Logger log = LoggerFactory.getLogger(BankAuthorService.class);
```
Think to use the corresponding class to instantiate it! 

#### What about log levels?

Use the most appropriate log level

The log level is a fundamental concept in logging. Whether the logging framework you use, it allows you to tag log records according to their severity or importance. 
For instance, [SLF4J](https://www.slf4j.org/) offers the [following log levels by default](https://www.slf4j.org/apidocs/org/slf4j/event/Level.html):

* ``TRACE`` : typically used to provide detailed diagnostic information that can be used for troubleshooting and debugging. Compare to DEBUG messages, TRACE messages are more fine-grained and verbose.
* ``DEBUG``: used to provide information that can be used to diagnose issues especially those related to program state.
* ``INFO``: used to record events that indicate that program is functioning normally.
* ``WARN``: used to record potential issues in your application. They may not be critical but should be investigated.
* ``ERROR``: records unexpected errors that occur during the operation of your application. In most cases, the error should be addressed as soon as possible to prevent further problems or outages.


#### ``AMOUNT_EXCEEDED`` issue

📝 Go the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/bank/BankAuthorService.java`` class and modify the following code block

```java
@Retry(name = "BankAuthorService", fallbackMethod = "acceptByDelegation")
public boolean authorize(PaymentProcessingContext context) {
 log.info("Authorize payment for {}", context);
 try {
  var response = client.authorize(initRequest(context));
  context.bankCalled = true;
  context.authorId = Optional.of(response.authorId());
  context.authorized = response.authorized();
  return context.authorized;
 } catch (Exception e) {
  log.warn("Should retry or fallback: {}", e.getMessage());
  throw e;
 }
}
```

Add a log before the `return context.authorized;` instruction to provide contextual information such as the authorId and the result of the call (authorized or not).

For the lazy, you can write something like that:
```java
[...]
context.bankCalled = true;
context.authorId = Optional.of(response.authorId());
context.authorized = response.authorized();
log.info("Bank answered payment authorization with authorId={}, authorized={}", response.authorId(), response.authorized());
return context.authorized;
```

#### ``INVALID_CARD_NUMBER`` issue
📝 Go to the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/PaymentService.java`` class and modify the following block code in the `process` method in the same way:

```java
private void process(PaymentProcessingContext context) {
[...]

 if (!cardValidator.checkCardNumber(context.cardNumber)) {
  context.responseCode = PaymentResponseCode.INVALID_CARD_NUMBER;
  return;
 }

[...]

```

For this error, you can log the error with the following content:

* The attributes of the ``PaymentProcessingContext `` 
* An error message

📝 You can also add more logs, especially in the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/CardValidator.java`` class.  
You may simply uncomment all the commented logs lines (``// LOG.…``), such as:

* In the ``CarValidator.checkLunKey()`` method, add a warning message when the key is not valid. For instance:

```java
LOG.warn("Check card number Luhn key does not pass: {}", cardNumber);
```

* In the ``CarValidator.checkExpiryDate()`` method, add a warning message when a ``DateTimeParseException`` is thrown. For instance:

```java
LOG.warn("Check card expiry date does not pass: bad format: {}", expiryDate);
```

* And do the same for other logs…

You can go further and add as many logs as you think would help in production 😎

#### Additional logs

We can also log payment requests and responses to provide even more context, which could be helpful for following requests in this workshop.

📝 Modify the `easypay-service/src/main/java/com/worldline/easypay/payment/boundary/PaymentResource.java` class by uncommenting all `// LOG.…` lines (keep MDC lines for later 😉).

### Check your code

🛠️ You can redeploy the `easypay-service` with the following commands:

```bash
$ docker compose build easypay-service
$ docker compose up -d easypay-service
```

🛠️ Now you can run the same commands ran earlier and check again the logs (`http POST…`).

### A technical issue

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

📝 To find the root cause, add first a _smart_ log entry in the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/PosValidator.java`` class. In the ``isActive()`` method, catch the exception and trace the error:

```java
public boolean isActive(String posId) {
    PosRef probe = new PosRef();
    probe.posId = posId;
    try {
        List<PosRef> posList = posRefRepository.findAll(Example.of(probe));

        if (posList.isEmpty()) {
            log.warn("checkPosStatus NOK, unknown posId {}", posId);
            return false;
        }

        boolean result = posList.get(0).active;

        if (!result) {
            log.warn("checkPosStatus NOK, inactive posId {}", posId);
        }
        return result;
    } catch (NullPointerException e) {
        log.warn("Invalid value for this POS: {}", posId);
        throw e;
    }
}
```

This is most likely an issue with some data in the database… Thanks to logs, we may quickly get the idea that the problem comes from the point of sale with ID `POS-02`.

It is easy to identify the issue if we don’t have traffic against our application, but what if we have more realistic traffic?

🛠️ Generate some load with `k6` (a Grafana tool for load testing):

```bash
$ k6 run -u 5 -d 30s k6/01-payment-only.js
```

👀 Check again logs:

```bash
$ docker compose logs -f easypay-service
```

Logs are now interleaved due to several requests being executed concurrently. It becomes harder to identify which point of sale is causing this error...

👀 If you look into the SQL import file (`easypay-service/src/main/resources/db/postgresql/data.sql`), you'll notice a `NULL` value instead of a boolean for the `active` column.

> aside negative
>
> Let’s keep the issue as it is for now.

### Using Mapped Diagnostic Context (MDC) to get more insights
> aside positive
>
>  Mapped Diagnostic Context (MDC) will help us add more context on every log output. For more information, refer to this web page: [https://logback.qos.ch/manual/mdc.html](https://logback.qos.ch/manual/mdc.html). 


📝 Go to the ``PaymentResource`` class and modify the method ``processPayment()`` to instantiate the [MDC](https://logback.qos.ch/manual/mdc.html):

```java
public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest)
  MDC.put("CardNumber",paymentRequest.cardNumber());
  MDC.put("POS",paymentRequest.posId());

  [...]
  MDC.clear();
return httpResponse;

```

👀 Go to the MDC spring profile configuration file (``easypay-service/src/main/resources/application-mdc.properties``) and check the configuration of both the ``CardNumber`` & ``POS``fields.

```properties
[...] %clr(CardNumber=){faint}%clr(%X{CardNumber:-null}) %clr(POS=){faint}%clr(%X{POS:-null}) [...] 
```

> aside positive
>
> `%X` allows to print the content of a MDC map.

📝 Activate the ``mdc`` profile in the ``compose.yml`` file:

```yaml
  easypay-service:
    image: easypay-service:latest
    [...]
      SPRING_PROFILES_ACTIVE: default,docker,mdc
```

🛠️ Rebuild and redeploy the `easypay-service`:

```bash
$ docker compose build easypay-service
$ docker compose up -d easypay-service
```

### Adding more content in our logs

🛠️ To have more logs, we will run several HTTP requests using [K6](https://k6.io/). Run the following command:

```bash
$ k6 run -u 5 -d 5s k6/01-payment-only.js
```
👀 Check then the logs to pinpoint some exceptions. 

### Logs Correlation

> aside positive
>
> You are probably wondering how to smartly debug in production when you have plenty of logs for several users and by the way different transactions?
>
> One approach would be to correlate all of your logs using a correlation Id.
> If an incoming request has no correlation id header, the API creates it. If there is one, it uses it instead.

### Let's dive into our logs on Grafana!

Logs are stored in the `logs` folder. You should find a `.log.json` file for each service in our application.  
We have configured our Spring Boot applications to output logs:

* In the console (the one we can see),
* But also in a file with JSON format.

> aside positive
> 
> Structured logging may be less readable for humans, but it is perfect for log concentrators as they are easy to parse!  
> You can look at a `logback-spring.xml` file to see the configuration we used (in the `src/main/resources` directory of one of the modules).

👀 You can take a look at log files such as `easypay-service.log.json` to see their structure: a lot of information is available!

In this section we will use [Promtail to broadcast them to Loki](https://grafana.com/grafana/dashboards/14055-loki-stack-monitoring-promtail-loki/) through [Grafana Alloy (OTEL Collector)](https://grafana.com/docs/alloy/latest/).  
[Loki](https://grafana.com/oss/loki/), our storage backend, is listening on host `loki` and port `3100`.

#### Configure Grafana Alloy collector

📝 Add the following Logging configuration to the ``docker/alloy/config.alloy`` file:

```json
////////////////////
// LOGS
////////////////////

// JSON LOG FILES (1)
local.file_match "jsonlogs" {
	path_targets = [{"__path__" = "/logs/*.json", "exporter" = "JSONFILE"}]
}
// (2)
loki.source.file "jsonlogfiles" {
	targets    = local.file_match.jsonlogs.targets
	forward_to = [loki.process.jsonlogs.receiver]
}
// (3)
loki.process "jsonlogs" {
	forward_to = [loki.write.endpoint.receiver]

  // (4)
	stage.json {
		expressions = {
			timestamp   = "timestamp",
			application = "context.properties.applicationName",
			instance    = "context.properties.instance",
      level       = "level",
		}
	}

  // (5)
	stage.labels {
		values = {
			application = "application",
			instance    = "instance",
      level       = "level,
		}
	}

  // (6)
	stage.timestamp {
		source = "timestamp"
		format = "RFC3339"
		fallback_formats = ["UnixMs",]
	}
}

// (7)
// EXPORTER (LOKI)
loki.write "endpoint" {
	endpoint {
		url = "http://loki:3100/loki/api/v1/push"
	}
}
```
Thanks to this configuration, the JSON files are automatically grabbed and broadcast to Loki.
Here is a short summary of the following steps:

1. List the input file we are interested in (all `.json` files in the `logs` directory)
2. Reads log entries from files defined in (1) and forward them to Alloy’s Loki components
3. Process log entries to parse them, extract data, transform data, and filter log entries
4. Extract contextual information from JSON log entry, such as the application name
5. Follow up the previous action by applying labels which will be available in Loki
6. Extract timestamp from the log file instead of using Promtail scrape time,
7. Define the output to Loki

🛠️ Restart the Grafana Alloy collector:

```bash
$ docker compose restart collector
```

👀 Open a browser page to Alloy UI (`port 12345`):

* Check out first the page ``http://localhost:12345/graph``,
* Select the ``loki.source.file`` component named `jsonlogfiles`,
* Check all the targets.

#### Explore logs with Grafana

> aside positive
>
> For this workshop, we have already configured the Loki datasource in Grafana.  
> You can take a look at its configuration in Grafana (port `3000`) by navigating to the `Connections` > `Data sources` section.  
> We only setup the Loki server url.

🛠️ Go to Grafana (port `3000`):
* Open an `Explore` dashboard,
* Select the Loki datasource.

Grafana offers a form to help you build your queries:
* Filtering labels,
* Finding text in logs,
* Parsing logs to extract and filter values,
* ...

You can also use a dedicated query language to make your queries directly: this is named [LogQL](https://grafana.com/docs/loki/latest/query/).


🛠️ Let’s get logs from the `easypay-service`:
* In the `Label filter`, select the application as ``easypay-service``,
* Click on ``Run Query``,
* Check out logs on the bottom of the view and unfold some of them.

Now we want to display logs in a table format with the timestamp, the log level and the message.  
In order to do that, we should extract the message from the JSON as it is not done automatically.

🛠️ Modify the query such as:
* Keep the same `Label filters` for the application `easypay-service`,
* Click on `+ Operations` and select `Formats` > `Json`: this will let us parse the log as JSON,
* Click on `+ Expression` to extract the JSON `message` field,
* Type `message="message"` to extract the value in a field with the same name,
* Click on `Run query` and unfold a log: you should see a new field named `message` in the `Fields` group.

🛠️ Time to format:
* Click on `Table` just below the `Logs volume` graph, to the right,
* Select the `Time`, `level` and `message` fields.

🛠️ Do not hesitate to hit the easypay payment endpoint with curl/httpie or k6 to generate some logs (whichever you prefer):

```bash
http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
# OR
k6 run -u 1 -d 2m k6/01-payment-only.js
```

👀 You can also view traces for the other services (e.g., ``api-gateway``).

👀 You can also search logs based on the correlation ID.

Maybe another issue? Do you see the card numbers? 😨

### Personal Identifiable Information (PII) obfuscation
For compliance and to prevent personal data loss, we will obfuscate the card number in the logs.

🛠️ In the Alloy configuration file (``docker/alloy/config.alloy``), add a [luhn stage](https://grafana.com/docs/alloy/latest/reference/components/loki.process/#stageluhn-block) at the end of the `loki.process "jsonlogs"` block definition:

```hcl
loki.process "jsonlogs" {
  //...
  stage.luhn {
    min_length  = 13
    replacement = "**MASKED**"
  }
}
``` 

🛠️ Restart the collector: 

```bash
$ docker compose restart collector
```

🛠️ Generate some logs with curl/httpie or k6.

✅ Check the card numbers are now obfuscated with the ``**MASKED**`` content. 

> aside positive
>
> During this workshop, we will only obfuscate the card numbers in Loki. It will therefore be stored as is in the log files but obfuscated in Loki and by this way in the data exposed on Grafana.

## Metrics

Let’s take control of our application’s metrics!

> aside positive
>
> EasyPay is already configured to expose metrics to Prometheus format with 
> [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)
> and [Micrometer](https://micrometer.io/).

### Metrics exposed by the application

🛠️ Check out the ``easypay-service`` metrics definitions exposed by Spring Boot Actuator first:

```bash
$ http :8080/actuator/metrics
```

👀 Explore the output.

🛠️ Now get the Prometheus metrics using this command:

```bash
$ http :8080/actuator/prometheus
```

This is an endpoint exposed by Actuator to let the Prometheus server get your application metrics.

### How are metrics scraped?

👀 Check out the Prometheus (``docker/prometheus/prometheus.yml``) configuration file.
All the scraper's definitions are configured here.

> aside positive
>
> Prometheus was already configured to scrape metrics for this workshop.
> Let's explore its configuration!

For instance, here is the configuration of the `config-server`:

```yaml
  - job_name: prometheus-config-server
    scrape_interval: 5s
    scrape_timeout: 5s
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - config-server:8890
```

You can see it uses the endpoint we looked into earlier under the hood.
But it is a static configuration: we should tell Prometheus where to look for metrics...

Hopefully, Prometheus is also able to query our ``discovery-server`` (Eureka service discovery) for discovering what are all the plugged services of our application. It will then scrape them in the same way:

```yaml
  # Discover targets from Eureka and scrape metrics from them
  - job_name: eureka-discovery
    scrape_interval: 5s
    scrape_timeout: 5s
    eureka_sd_configs:
      - server: http://discovery-server:8761/eureka (1)
        refresh_interval: 5s
    relabel_configs: (2)
      - source_labels: [__meta_eureka_app_instance_metadata_metrics_path]
        target_label: __metrics_path__
```
1. We plugged Prometheus to our Eureka `discovery-server` to explore all the metrics of the underlying systems
2. Configuration allows additional operations, such as relabelling the final metric before storing it into Prometehus

👀 You can have an overview of all the scraped applications on the Prometheus dashboard:

* Go to ``http://localhost:9090`` if you started the stack locally, or use the link provided by GitPod in the `PORTS` view for port `9090`,
* Click on ``Status`` > ``Targets``,
* Explore the different services discovered in the ``eureka-discovery`` section:
  * You should not see the ``easypay-service``...
  * ... but rest assured, we will fix that!

### Add scrape configuration for our easypay service

📝 Modify the ``docker/prometheus/prometheus.yml`` file to add a new configuration to scrape the easypay service.
You can use the ``prometheus-config-server`` configuration as a model:

* Job name: ``prometheus-easypay-service``
* Scrape interval and timeout: ``5s``
* Metrics path: ``/actuator/prometheus``
* Target: ``easypay-service:8080``

That should give you the following yaml configuration:

```yaml
  - job_name: prometheus-easypay-service
    scrape_interval: 5s
    scrape_timeout: 5s
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - easypay-service:8080
```

🛠️ Restart the Prometheus server to take into account this new configuration:

```bash
$ docker compose restart prometheus
```

👀 Now explore again the targets (``Status`` > ``Targets``) on the Prometheus dashboard (``port 9090``). 

> aside positive
>
> You should find the new target for easypay with the ``UP`` state under the ``prometheus-easypay-service`` job group!  
> It is now time to explore these metrics.

### Let's explore the metrics

> aside positive
>
> For this workshop, we have already configured in Grafana the Prometheus datasource.  
> You can have a look at its configuration in Grafana (``port 3000``) in the ``Connections`` > ``Data sources`` section.  
> It is pretty straightforward as we have only setup the Prometheus server URL.

🛠️ Go to Grafana and start again an ``Explore`` dashboard.

🛠️ Select the ``Prometheus`` datasource instead of the ``Loki`` one.

In this section you will hand on the metrics query builder of Grafana.

The ``Metric`` field lists all the metrics available in the Prometheus server: take time to explore them.

🛠️ For example, you can select the metric named ``jvm_memory_used_bytes``, and click on the ``Run query`` button to plot the memory usage of all your services by memory area,

🛠️ If you want to plot the total memory usage of your services:
  * Click on ``Operations`` and select ``Aggregations`` > ``Sum``, and ``Run query``: you obtain the whole memory consumption of all your JVMs,
  * To split the memory usage per service, you can click on the ``By label`` button and select the label named ``application`` (do not forget to click on ``Run query`` afterthat).

🛠️ You can also filter metrics to be displayed using ``Label filters``: try to create a filter to display only the metric related to the application named easypay-service.

> aside positive
>
> At the bottom of the query builder, you should see something like:  
> `sum by(application) (jvm_memory_used_bytes{application="easypay-service"})`.  
> This is the effective query raised by Grafana to Prometheus in order to get its metrics.  
> This query language is named [PromQL](https://prometheus.io/docs/prometheus/latest/querying/basics/).

### Dashboards

With Grafana, you can either create your own dashboards or import some provided by the community from Grafana’s website.

We will choose the second solution right now and import the following dashboards:
* [JVM Micrometer](https://grafana.com/grafana/dashboards/12271-jvm-micrometer/), which ID is `12271`,
* [Spring Boot JDBC & HikariCP](https://grafana.com/grafana/dashboards/20729-spring-boot-jdbc-hikaricp/), which ID is `20729`.

🛠️ To import these dashboards:
* Go to Grafana (``port 3000``), and select the ``Dashboards`` section on the left,
* Then click on ``New`` (top right), and click on ``Import``,
* In the ``Find and import…`` field, just paste the ID of the dashboard and click on ``Load``,
* In the ``Select a Prometheus data source``, select ``Prometheus`` and click on ``Import``,
* You should be redirected to the newly imported dashboard.

> aside positive
>
> Imported dashboards are available directly from the ``Dashboards`` section of Grafana.

👀 Explore the ``JVM Micrometer`` dashboard: it works almost out of box.  
It contains a lot of useful information about JVMs running our services.

The ``application`` filter (top of the dashboard) let you select the service you want to explore metrics.

### Incident!

🛠️ Now let's simulate some traffic using [Grafana K6](https://k6.io/). Run the following command: 

```bash
$ k6 run -u 5 -d 2m k6/02-payment-smartbank.js
```

👀 Go back to the Grafana dashboard, click on ``Dashboards`` and select ``JVM Micrometer``:

* Explore the dashboard for the ``easypay-service``, especially the Garbage collector and CPU statistics.

* Look around the other ``Spring Boot JDBC & HikariCP`` dashboard then and see what happens on the database connection pool for ``easypay-service``.

We were talking about an incident, isn’t it? 

👀 Let's go back to the Explore view of Grafana, select Loki as a data source and see what happens!

🛠️ Create a query with the following parameters to get error logs of the ``smartbank-gateway`` service:

* Label filters: ``application`` = ``smartbank-gateway``
* line contains/Json: ``expression``= ``level="level"``
* label filter expression: ``label`` = ``level ; ``operator`` = ``=~`` ; ``value`` = ``WARN|ERROR`` 

🛠️ Click on ``Run query`` and check out the logs.

Normally you would get a ``java.lang.OutOfMemoryError`` due to a saturated Java heap space.

👀 To get additional insights, you can go back to the JVM dashboard and select the ``smartbank-gateway`` application.

Normally you will see the used JVM Heap reaching the maximum allowed.

> aside positive
>
> Grafana and Prometheus allows you to generate alerts based on metrics, using [Grafana Alertmanager](https://grafana.com/docs/grafana/latest/alerting/set-up/configure-alertmanager/).  
> For instance, if CPU usage is greater than 80%, free memory is less than 1GB, used heap is greater than 80%, etc.

### Business metrics

Observability is not only about incidents. You can also define your own metrics.

[Micrometer](https://micrometer.io/), the framework used by Spring Boot Actuator to expose metrics, provides an API to create your own metrics quite easily:
* [Counters](https://docs.micrometer.io/micrometer/reference/concepts/counters.html): value which can only increment (such as the number of processed requests),
* [Gauges](https://docs.micrometer.io/micrometer/reference/concepts/gauges.html): represents the current value (such as the speed gauge of a car),
* [Timers](https://docs.micrometer.io/micrometer/reference/concepts/timers.html): measures latencies and frequencies of an event (such as response times).

Let’s go back to our code!

#### Objectives

We want to add new metrics to the easypay service to measure they payment processing and store time.  
So we target a metric of **Timer** type.

In order to achieve this goal, we will measure the time spent in the two methods `process` and `store` of the `com.worldline.easypay.payment.control.PaymentService` class of the `easypay-service` module.  
This class is the central component responsible for processing payments: it provides the ``accept`` public method, which delegates its responsibility to two private ones:
* ``process``: which does all the processing of the payment: validation, calling third parties…
* ``store``: to save the processing result in database.

We also want to count the number of payment requests processed by our system. We will use a metric of **Counter** type.

> aside negative
>
> Micrometer provides the ``@Timed`` annotation to simplify the creation of such metric.  
> Unfortunately, [it does not work with Spring Boot](https://docs.micrometer.io/micrometer/reference/concepts/timers.html#_the_timed_annotation) outside ``Controller`` components, on arbitrary methods.  
> So let’s do it "manually".

You can take a look at the [Micrometer’s documentation about Timers](https://docs.micrometer.io/micrometer/reference/concepts/timers.html).

#### 1. Declare the timers

We need to declare two timers in our code:
* ``processTimer`` to record the ``rivieradev.payment.process`` metric: it represents the payment processing time and record the time spent in the `process` method,
* ``storeTimer`` to record the ``rivieradev.payment.store`` metric: it represents the time required to store a payment in database by recording the time spent in the `store` method.

📝 Let’s modify the ``com.worldline.easypay.payment.control.PaymentService`` class to declare them:

```java
// ...
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class PaymentService {
    // ...
    private Timer processTimer; (1)
    private Timer storeTimer;

    public PaymentService(
            // ...,
            MeterRegistry meterRegistry) { (2)
        // ...

        processTimer = Timer (3)
                .builder("rivieradev.payment.process") (4)
                .description("Payment processing time") (5)
                .register(meterRegistry); (6)
        storeTimer = Timer
                .builder("rivieradev.payment.store")
                .description("Payment store time")
                .register(meterRegistry);
    }
```
1. Declare the two timers,
2. Injects the ``MeterRegistry`` provided by Spring Boot Actuator in the class constructor, as it is required to initialize the timers,
3. Initialize the two timers by giving them a name (4), a description (5) and adding them to the meter registry.

#### 2. Record time spent in the methods

The ``Timer`` API [allows to record blocks of code](https://docs.micrometer.io/micrometer/reference/concepts/timers.html#_recording_blocks_of_code). It’s going to be our way:

```java
timer.record(() -> {
  // some code
});
```

📝 Let’s modify our `process` and `store` methods to record our latency with the new metrics.  
We can simply wrap our original code in a Java `Runnable` functional interface:

```java
    // ...
    private void process(PaymentProcessingContext context) {
        processTimer.record(() -> { (1)
            if (!posValidator.isActive(context.posId)) {
                context.responseCode = PaymentResponseCode.INACTIVE_POS;
                return;
            }
            // ...
        });
    }

    private void store(PaymentProcessingContext context) {
        storeTimer.record(() -> { (2)
            Payment payment = new Payment();
            // ...
        });
    }
```
1. Modify the ``process`` method to wrap all its content into a ``Runnable`` consumed by the `record` method of our ``processTimer`` timer,
2. Do the same for the `store` method.

#### 3. Add counter

📝 Let’s do the same for the counter:

```java
// ...
import io.micrometer.core.instrument.Counter;

@Service
public class PaymentService {
    //...
    private Counter requestCounter; (1)

    public PaymentService(
            //...
            ) {
        // ...
        requestCounter = Counter (2)
                .builder("rivieradev.payment.requests")
                .description("Payment requests counter")
                .register(meterRegistry);
    }
```
1. Declares the counter,
2. Initializes the counter.

📝 The method ``accept`` of the ``PaymentService`` class is invoked for each payment request, it is a good candidate to increment our counter:

```java
    @Transactional(Transactional.TxType.REQUIRED)
    public void accept(PaymentProcessingContext paymentContext) {
        requestCounter.increment(); (1)
        process(paymentContext);
        store(paymentContext);
        paymentTracker.track(paymentContext);
    }
```
1. Increment the counter each time the method is invoked.

#### 4. Redeploy easypay

🛠️ Rebuild the easypay-service:

```bash
$ docker compose build easypay-service
```

🛠️ Redeploy easypay:

```bash
$ docker compose up -d easypay-service
```

🛠️ Once easypay is started (you can check logs with the ``docker compose logs -f easypay-service`` command and wait for an output like ``Started EasypayServiceApplication in 32.271 seconds``):

* Execute some queries: 

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
```

* Go into the container and query the ``actuator/prometheus`` endpoint to look at our new metrics:

```bash
$ docker compose exec -it easypay-service sh
/ $ curl http://localhost:8080/actuator/prometheus | grep riviera
```

You should get this kind of output:

```
# HELP rivieradev_payment_process_seconds Payment processing time
# TYPE rivieradev_payment_process_seconds summary
rivieradev_payment_process_seconds_count{application="easypay-service",...} 4
rivieradev_payment_process_seconds_sum{application="easypay-service",...} 1.984019362
# HELP rivieradev_payment_process_seconds_max Payment processing time
# TYPE rivieradev_payment_process_seconds_max gauge
rivieradev_payment_process_seconds_max{application="easypay-service",...} 1.927278528
# HELP rivieradev_payment_store_seconds Payment store time
# TYPE rivieradev_payment_store_seconds summary
rivieradev_payment_store_seconds_count{application="easypay-service",...} 4
rivieradev_payment_store_seconds_sum{application="easypay-service",...} 0.299205989
# HELP rivieradev_payment_store_seconds_max Payment store time
# TYPE rivieradev_payment_store_seconds_max gauge
rivieradev_payment_store_seconds_max{application="easypay-service",...} 0.291785969
# HELP rivieradev_payment_requests_total Payment requests counter
# TYPE rivieradev_payment_requests_total counter
rivieradev_payment_requests_total{application="easypay-service",...} 4.0
```

When using a `Timer` you get three metrics by default, suffixed by:
* ``_count``: the number of hits,
* ``_sum``: the sum of time spent in the method,
* ``_max``: the maximum time spent in the method.

Especially we can get the average time spent in the method by dividing the sum by the count.

Finally, our ``Counter`` is the last metric suffixed with ``_total``.

#### 5. Add histograms and percentiles

As we are talking about latencies, you may be also interested in histograms to get the distribution of the events per buckets or percentiles values (the famous 0.99, 0.999…). Fortunately, ``Timers`` allow to compute [Histograms and Percentiles](https://docs.micrometer.io/micrometer/reference/concepts/histogram-quantiles.html)!

📝 Modify the two timers as follows:

```java
// ...
@Service
public class PaymentService {
    // ...
    public PaymentService(
            // ...
            ) {
        // ...

        processTimer = Timer
                .builder("rivieradev.payment.process")
                .description("Payment processing time")
                .publishPercentileHistogram() (1)
                .publishPercentiles(0.5, 0.90, 0.95, 0.99, 0.999) (2)
                .register(meterRegistry);
        storeTimer = Timer
                .builder("rivieradev.payment.store")
                .description("Payment store time")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.90, 0.95, 0.99, 0.999)
                .register(meterRegistry);
    }
```
1. Configures the ``Timer`` to publish a histogram allowing to compute aggregable percentiles server-side,
2. Exposes percentiles value computed from the application: **these value are not aggregable!**

🛠️ Repeat the step 4 (redeploy `easypay-service` and get Prometheus metrics in-container).

You should get way more metrics, especially a new one type suffixed with `_bucket`:

```
# HELP rivieradev_payment_process_seconds Payment processing time
# TYPE rivieradev_payment_process_seconds histogram
rivieradev_payment_process_seconds_bucket{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local",le="0.001"} 0
// ...
rivieradev_payment_process_seconds_bucket{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local",le="30.0"} 0
rivieradev_payment_process_seconds_bucket{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local",le="+Inf"} 0
rivieradev_payment_process_seconds_count{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local"} 0
rivieradev_payment_process_seconds_sum{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local"} 0.0
# HELP rivieradev_payment_process_seconds_max Payment processing time
# TYPE rivieradev_payment_process_seconds_max gauge
rivieradev_payment_process_seconds_max{application="easypay-service",instance="easypay-service:a44149cd-937a-4e96-abc2-0770343e49bc",namespace="local"} 0.0
```

ℹ️ Each ` bucket ` contains the number of event which lasts less than the value defined in the ``le`` tag.

#### 6. Visualization

🛠️ Go back to Grafana (`port 3000`), and go into the ``Dashboards`` section.

🛠️ We will import the dashboard defined in the ``docker/grafana/dashboards/easypay-monitoring.json`` file:
* Click on ``New`` (top right), and select ``Import``,
* In the ``Import via dashboard JSON model`` field, paste the content of the ``easypay-monitoring.json``  file and click on ``Load``,
* Select Prometheus as a data source.

You should be redirected to the ``Easypay Monitoring`` dashboard.

It provides some dashboards we have created from the new metrics you exposed in your application:

* `Payment request count total (rated)`: represents the number of hit per second in our application computed from our counter,
* ``Payment Duration distribution``: represents the various percentiles of our application computed from the ``rivieradev_payment_process`` timer and its histogram,
* ``Requests process performance`` and ``Requests store performance``: are a visualization of the buckets of the two timers we created previously.

🛠️ You can generate some load to view your dashboards evolving live:

```bash
$ k6 -u 2 -d 2m k6/01-payment-only.js
```

> aside positive
>
> Do not hesitate to explore the way the panels are created, and the queries we used!  
> Just hover the panel you are interested in, click on the three dots and select Edit.

## Traces

In this section, we'll explore **distributed tracing**, the third pillar of application observability.

Distributed tracing is an essential tool for monitoring and analyzing the performance of complex applications. It tracks the flow of requests across multiple services and components, helping to identify bottlenecks and improve efficiency — particularly useful for intricate systems like Easypay.

With Spring Boot, there are a couple of approaches to incorporate distributed tracing into your application:
* Utilize the [Spring Boot Actuator integration](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.tracing) with support from [Micrometer Tracing](https://docs.micrometer.io/docs/tracing),
* Or adopt a broader [Java Agent approach](https://github.com/open-telemetry/opentelemetry-java-instrumentation) provided by the OpenTelemetry project, which automatically instruments our code when attached to our JVM.

For this workshop, we'll use the Java Agent method and, with a focus on Grafana, we will employ their version of the [OpenTelemetry Java Agent](https://github.com/grafana/grafana-opentelemetry-java).

The Grafana Alloy collector will be used once again, tasked with receiving traces and forwarding them to the Tempo backend.

> aside positive
>
> Utilizing collectors offers several advantages for managing telemetry data:
> - Reduces the need for complicated application configurations: just send data to `localhost`,
> - Centralizes configuration to a single point: the collector,
> - Acts as a buffer to prevent resource overuse,
> - Can transform data before ingestion,
> - Supports data intake from various protocols and can relay them to any backend,
> - ...

Lastly, we will use Grafana to examine and interpret these traces, allowing us to better understand and optimize our application's performance.

### Enable distributed tracing

To capture the entire transaction across all services in a trace, it's essential to instrument all the services in our application.

> aside positive
>
> In this workshop, our primary focus will be on the `easypay` service.
> For efficiency, we have already instrumented the other services beforehand.

#### Download Grafana Opentelemetry Java Agent

If you're using *GitPod*, the Java Agent should already be available in the `instrumentation/grafana-opentelemetry-java.jar` directory.

🛠️ If you are participating in this workshop on your workstation, or if the file is missing, you can run the following script to download it:

```bash
$ bash -x scripts/download-agent.sh
```

#### Enable Java Agent

📝 Since we are deploying the easypay-service using *Docker*, we need to modify the last lines of the `easypay-service/src/main/docker/Dockerfile`:

```Dockerfile
# ...
USER javauser

# Copy Java Agent into the container
COPY instrumentation/grafana-opentelemetry-java.jar /app/grafana-opentelemetry-java.jar

# Add the -javagent flag to setup the JVM to start with our Java Agent
ENTRYPOINT ["java", "-javaagent:/app/grafana-opentelemetry-java.jar", "-cp","app:app/lib/*","com.worldline.easypay.EasypayServiceApplication"] # (2)
```

The `ENTRYPOINT` instruction specifies the default command that will be executed when the container starts.

🛠️ You can now build the updated easypay-service container image:

```bash
$ docker compose build easypay-service
```

#### Configure Grafana Alloy

It's time to set up *Grafana Alloy* for handling telemetry data. We will configure it to accept traces through the OpenTelemetry GRPC protocol (OTLP) on port `4317`, and then forward them to *Grafana Tempo*, which listens on the host `tempo` on the same port `4317` (this setup specifically handles OTLP traces).

📝 Please add the following configuration to the `docker/alloy/config.alloy` file:

```terraform
// ...

// RECEIVER SETUP (OTLP GRPC) (1)
otelcol.receiver.otlp "default" {
	grpc {
		endpoint = "0.0.0.0:4317"
	}

	output {
		traces  = [otelcol.processor.batch.default.input]
	}
}

// BATCH PROCESSING FOR OPTIMIZATION (2)
otelcol.processor.batch "default" {
	output {
		traces  = [otelcol.exporter.otlp.tempo.input]
	}
}

// TRACE EXPORTING TO TEMPO (OTLP) (3)
otelcol.exporter.otlp "tempo" {
	client {
		endpoint = "tempo:4317"

		tls {
			insecure = true
		}
	}
}
```
1. Setting up the [``otelcol.receiver.otlp``](https://grafana.com/docs/alloy/latest/reference/components/otelcol.receiver.otlp/) receiver to accept telemetry data over the OTEL protocol via GRPC, listening on port `4317`,
2. Configuring the [processor](https://grafana.com/docs/alloy/latest/reference/components/otelcol.processor.batch/) to batch traces efficiently, reducing resource usage,
3. Establishing the [``otelcol.exporter.otlp``](https://grafana.com/docs/alloy/latest/reference/components/otelcol.exporter.otlp/) exporter to send collected telemetry data to the Grafana Tempo service.

ℹ️ The Grafana OpenTelemetry Java Agent is pre-configured to transmit telemetry data directly to the collector. This setup is facilitated through environment variables specified in the `compose.yml` file:

```yaml
services:
  # ...
  easypay-service:
    # ..
    environment:
      # ...
      OTEL_SERVICE_NAME: easypay-service (1)
      OTEL_EXPORTER_OTLP_ENDPOINT: http://collector:4317 (2)
      OTEL_EXPORTER_OTLP_PROTOCOL: grpc (3)
    # ...
```
1. `OTEL_SERVICE_NAME` defines a service name which will be attached to traces to identify the instrumented service,
2. `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable configures where the telemetry data should be sent,
3. `OTEL_EXPORTER_OTLP_PROTOCOL` sets the OTLP protocol used behind, here GRPC (can be HTTP).

> aside positive
>
> Find more information about how to configure the OpenTelemetry Java Agent in [its official documentation](https://opentelemetry.io/docs/languages/java/configuration/).

🛠️ To apply the new settings, restart Grafana Alloy with the following command:

```bash
$ docker compose restart collector
```

✅ After restarting, verify that Grafana Alloy is up and running with the updated configuration by accessing the Alloy dashboard on port ``12345``.

🛠️ Redeploy the updated ``easypay-service``:

```bash
$ docker compose up -d easypay-service
```

✅ To ensure easypay-service has started up correctly, check its logs with:

```bash
$ docker compose logs -f easypay-service
```

#### Explore Traces with Grafana

> aside positive
>
> For this workshop, we've already configured the Tempo datasource in Grafana.
> You can take a look at its configuration in Grafana (available on port ``3000``) by navigating to the `Connections` > `Data sources` section.
> Similar to Prometheus, the configuration is quite straightforward as we only need to set up the Tempo server URL.

🛠️ Generate some load on the application to produce traces:

```bash
$ k6 run -u 1 -d 5m k6/01-payment-only.js
```

🛠️ Let’s explore your first traces in Grafana:
* Go to Grafana and open an ``Explore`` dashboard,
* Select the `Tempo` data source and click on ``Run query`` to refresh the view.

> aside negative
>
> You may need to wait one or two minutes to allow Tempo to ingest some traces…

👀 Click on `Service Graph` and explore the `Node graph`: this view is extremely helpful for visualizing and understanding how our services communicate with each other.

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
* Click on `Node graph` to get a graphical view of all the spans participating in the trace.

🛠️ Continue your exploration in the `Search` pane:
* For example, you can add the `Status` `=` `error` filter to see only traces that contain errors.

### Sampling

When we instrument our services using the agent, every interaction, including Prometheus calls to the `actuator/prometheus` endpoint, is recorded.

To avoid storing unnecessary data in Tempo, we can sample the data in two ways:
* [Head Sampling](https://opentelemetry.io/docs/concepts/sampling/#head-sampling)
* [Tail Sampling](https://opentelemetry.io/docs/concepts/sampling/#tail-sampling)

In this workshop, we will implement Tail Sampling.

Modify the Alloy configuration file (``docker/alloy/config.alloy``) as follows:
```
// ...
// RECEIVER (OTLP)
otelcol.receiver.otlp "default" {
	grpc {
		endpoint = "0.0.0.0:4317"
	}

	output {
		traces  = [otelcol.processor.tail_sampling.actuator.input] // (1)
	}
}

// TAIL SAMPLING (2)
otelcol.processor.tail_sampling "actuator" {
  // Filter on http.url attribute (3)
	policy {
		name = "filter_http_url"
		type = "string_attribute"
		string_attribute {
			key = "http.url"
			values = ["/actuator/health", "/actuator/prometheus"]
			enabled_regex_matching = true
			invert_match = true
		}
	}

  // Filter on url.path attribute (3)
	policy {
		name = "filter_url_path"
		type = "string_attribute"
		string_attribute {
			key = "url.path"
			values = ["/actuator/health", "/actuator/prometheus"]
			enabled_regex_matching = true
			invert_match = true
		}
	}

	output {
		traces = [otelcol.processor.batch.default.input] // (4)
	}
}
```
1. Modify the output of the `otelcol.receiver.otlp` to export traces to the [otelcol.processor.tail_sampling](https://grafana.com/docs/alloy/latest/reference/components/otelcol.processor.tail_sampling/) component defined just after.
2. Create a new `otelcol.processor.tail_sampling` component.
3. Configure it with two policies based on span attributes.
4. Export non-filtered spans to the `otelcol.processor.batch` processor we defined previously.

This configuration will filter the [SPANs](https://opentelemetry.io/docs/concepts/signals/traces/#spans) created from `/actuator` API calls.

🛠️ Restart the Alloy collector:

```bash
$ docker compose restart collector
```

Starting from this moment, you should no longer see traces related to `actuator/health` or `actuator/prometheus` endpoints.

### Custom Traces

Just like metrics, it is also possible to add your own spans on arbitrary methods to provide more business value to the observability of your application.

Let’s return to our code!

#### Objectives

We want to add new spans to the traces generated in the `easypay-service` application to track payment processing and store events.

To achieve this goal, we will create new spans when the `process` and `store` methods of the `com.worldline.easypay.payment.control.PaymentService` class in the `easypay-service` module are invoked.

As a reminder, this class is the central component responsible for processing payments. It provides the public method `accept`, which delegates its responsibilities to two private methods:
* `process`: which handles all the processing of the payment, including validation and calling third parties.
* `store`: which saves the processing result in the database.

#### 1. Add Required Dependencies

We need to add the `io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations` dependency to our module to access some useful annotations.

👀 This has already been done in advance for this workshop. The following dependencies were added to the Gradle build file (`build.gradle.kts`) of the `easypay-service` module:

```kotlin
dependencies {
  //...

	// Add opentelemetry support
	implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.5.0")

  // ...
}
```

#### 2. Add Custom Spans

📝 To add new spans based on methods, we can simply use the `@WithSpan` Java annotation. When a traced transaction invokes the annotated method, a new span will be created. Here’s how to do it:

```java
// ...
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class PaymentService {
    // ...

    @WithSpan("Payment processing method")
    private void process(PaymentProcessingContext context) {
        //...
    }

    @WithSpan("Payment store method")
    private void store(PaymentProcessingContext context) {
        //...
    }
```

📝 We can also provide additional information to the span, such as method parameters using the ``@SpanAttribute`` annotation:

```java
// ...
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

@Service
public class PaymentService {
    // ...
    
    @WithSpan("RivieraDev: Payment processing method")
    private void process(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }

    @WithSpan("RivieraDev: Payment store method")
    private void store(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }
```

This will provide the whole PaymentProcessingContext into the trace.

#### 3. Build and redeploy

🛠️ As we did before:

```bash
$ docker compose build easypay-service
$ docker compose up -d easypay-service
```

#### 4. Test it!

🛠️ Generate some payments:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
```

👀 Go back to Grafana and try to find your new traces using what you've learned previously. Observe the spans you added.

> aside negative
>
> It may take some time for `easypay-service` to be registered in the service discovery and be available from the API gateway.  
> Similarly, your traces being ingested by Tempo might also take some time. Patience is key 😅

Starting from this moment, you should no longer see traces related to `actuator/health` or `actuator/prometheus` endpoints.

### Custom Traces

Just like metrics, it is also possible to add your own spans on arbitrary methods to provide more business value to the observability of your application.

Let’s return to our code!

#### Objectives

We want to add new spans to the traces generated in the `easypay-service` application to track payment processing and store events.

To achieve this goal, we will create new spans when the `process` and `store` methods of the `com.worldline.easypay.payment.control.PaymentService` class in the `easypay-service` module are invoked.

As a reminder, this class is the central component responsible for processing payments. It provides the public method `accept`, which delegates its responsibilities to two private methods:
* `process`: which handles all the processing of the payment, including validation and calling third parties.
* `store`: which saves the processing result in the database.

#### 1. Add Required Dependencies

We need to add the `io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations` dependency to our module to access some useful annotations.

👀 This has already been done in advance for this workshop. The following dependencies were added to the Gradle build file (`build.gradle.kts`) of the `easypay-service` module:

```kotlin
dependencies {
  //...

	// Add opentelemetry support
	implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
	implementation("io.opentelemetry:opentelemetry-api")
	implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.5.0")

  // ...
}
```

#### 2. Add Custom Spans

📝 To add new spans based on methods, we can simply use the `@WithSpan` Java annotation. When a traced transaction invokes the annotated method, a new span will be created. Here’s how to do it:

```java
// ...
import io.opentelemetry.instrumentation.annotations.WithSpan;

@Service
public class PaymentService {
    // ...

    @WithSpan("Payment processing method")
    private void process(PaymentProcessingContext context) {
        //...
    }

    @WithSpan("Payment store method")
    private void store(PaymentProcessingContext context) {
        //...
    }
```

📝 We can also provide additional information to the span, such as method parameters using the ``@SpanAttribute`` annotation:

```java
// ...
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

@Service
public class PaymentService {
    // ...
    
    @WithSpan("RivieraDev: Payment processing method")
    private void process(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }

    @WithSpan("RivieraDev: Payment store method")
    private void store(@SpanAttribute("context") PaymentProcessingContext context) { // <-- HERE
        // ...
    }
```

This will provide the whole PaymentProcessingContext into the trace.

#### 3. Build and redeploy

🛠️ As we did before:

```bash
$ docker compose build easypay-service
$ docker compose up -d easypay-service
```

#### 4. Test it!

🛠️ Generate some payments:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
```

👀 Go back to Grafana and try to find your new traces using what you've learned previously. Observe the spans you added.

> aside negative
>
> It may take some time for `easypay-service` to be registered in the service discovery and be available from the API gateway.  
> Similarly, your traces being ingested by Tempo might also take some time. Patience is key 😅

## Correlation

Grafana allows correlation between all our telemetry data:

* Logs with Traces,
* Metrics with Traces,
* Traces with Logs,
* Traces with Metrics,
* …

When discussing observability, correlation is essential. It enables you to diagnose the root cause of an issue quickly by using all the telemetry data involved.

### Logs and traces

🛠️ Let's go back to the Grafana `Explore` dashboard:
* Select the ``Loki`` data source,
* Add a label filter to select logs comming from ``easypay-service``,
* Run a query and select a log entry corresponding to a payment query.

👀 Now check to see if there is a ``mdc`` JSON element that includes both the [``trace_id``](https://www.w3.org/TR/trace-context/#trace-id) and [``span_id``](https://www.w3.org/TR/trace-context/#parent-id).
These will help us correlate our different request logs and traces.

> aside positive
>
> These concepts are part of the [W3C Trace Context Specification](https://www.w3.org/TR/trace-context/).

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:
* Select the `Loki` data source,
* Create a `Derived fields` configuration:
  * `Name`: `TraceID`
  * `Type`: `Regex in log line`
  * `Regex`: `"trace_id":"(\w+)"`
  * `Query`: `${__value.raw}`
  * `URL Label`: `View Trace`
  * Enable `Internal Link` and select `Tempo`.

✅ To validate the configuration, you can put an example log message in this view:
* Click on `Show example log message`,
* Paste a log line (non-formatted JSON), such as:

```json
{"sequenceNumber":0,"timestamp":1719910676210,"nanoseconds":210150314,"level":"INFO","threadName":"kafka-binder-health-1","loggerName":"org.apache.kafka.clients.NetworkClient","context":{"name":"default","birthdate":1719907122576,"properties":{"applicationName":"easypay-service","instance":"easypay-service:9a2ac3f0-c41e-4fcd-8688-123993f1d5db"}},"mdc": {"trace_id":"8b277041692baa8167de5c67977d6571","trace_flags":"01","span_id":"13ff9e44be450b8e"},"message":"[Consumer clientId=consumer-null-1, groupId=null] Node -1 disconnected.","throwable":null}
```

It should display a table:
* `Name`: `TraceID`
* `Value`: the trace ID from the log message
* `Url`: the same trace ID

🛠️ Go back to the Grafana `Explore` dashboard and try to find the same kind of log message:
* Expand the log,
* At the bottom of the log entry, you should find the `Fields` and `Links` sections,
* If the log contains a trace ID, you should see a button labeled `View Trace`,
* Click on this button!

👀 Grafana should open a pane with the corresponding trace from Tempo!

Now you can correlate logs and traces!  
If you encounter any exceptions in your error logs, you can now see where it happens and get the bigger picture of the transaction from the customer's point of view.

#### How was it done?

First of all, logs should contain the `trace_id` information.  
Most frameworks handle this for you. Whenever a request generates a trace or span, the value is placed in the MDC (Mapped Diagnostic Context) and can be printed in the logs.

On the other hand, Grafana has the ability to parse logs to extract certain values for the Loki data source. This is the purpose of `Derived fields`.

When configuring the Loki data source, we provided Grafana with the regex to extract the trace ID from logs and linked it to the Tempo data source. Behind the scenes, Grafana creates the bridge between the two telemetry data sources. And that’s all 😎

### Metrics and Traces (Exemplars)

Exemplars are annotations used in metrics that link specific occurrences, like logs or traces, to data points within a metric time series. They provide direct insights into system behaviors at moments captured by the metric, aiding quick diagnostics by showing related trace data where anomalies occur. This feature is valuable for debugging, offering a clearer understanding of metrics in relation to system events.

🛠️ Generate some load towards the `easypay-service`:

```bash
$ k6 run -u 1 -d 2m k6/01-payment-only.js
```

👀 Now, let's see how exemplars are exported by our service:
* Access the `easypay-service` container::

```bash
$ docker compose exec -it easypay-service sh
```

* Query actuator for Prometheus metrics, but in the OpenMetrics format:

```bash
$ curl http://localhost:8080/actuator/metrics -H 'Accept: application/openmetrics-text' | grep 'trace_id'
```

You should obtain metrics with the following format:

```
http_server_requests_seconds_bucket{application="easypay-service",error="none",exception="none",instance="easypay-service:39a9ae31-f73a-4a63-abe5-33049b8272ca",method="GET",namespace="local",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.027962026"} 1121 # {span_id="d0cf53bcde7b60be",trace_id="969873d828346bb616dca9547f0d9fc9"} 0.023276118 1719913187.631
```

The interesting part starts after the `#` character, this is the so-called exemplar:

```
               SPAN ID                           TRACE ID                     VALUE      TIMESTAMP
# {span_id="d0cf53bcde7b60be",trace_id="969873d828346bb616dca9547f0d9fc9"} 0.023276118 1719913187.631
```

That could be translated by:
* `easypay-service` handled an HTTP request,
* Which generated trace ID id `969873d828346bb616dca9547f0d9fc9`,
* Request duration was `0.023276118` second,
* At timestamp `1719913187.631`

👀 Exemplars can be analyzed in Grafana:
* Go to the Grafana `Explore` view,
* Select the `Prometheus` data source,
* Switch to the `Code` mode (button on the right),
* Paste the following PromQL query:

```
http_server_requests_seconds_count{application="easypay-service", uri="/payments"}
```

* Unfold the `Options` section and enable `Exemplars`,
* Click on `Run query`.

👀 In addition to the line graph, you should see square dots at the bottom of the graph:
* Hover over a dot,
* It should display useful information for correlation, particularly a `trace_id`.

#### Enable correlation

🛠️ In Grafana, go to the `Connections` > `Data sources` section:
* Select the `Prometheus` data source,
* Click on `Exemplars`:
  * Enable `Internal link` and select the Tempo data source,
  * `URL Label`: `Go to Trace`,
  * `Label name:`: `trace_id` (as displayed in the exemplar values),
* Click on `Save & test`.

🛠️ Go back to the Grafana `Explore` dashboard and try to find the same exemplar as before:
* Hover over it,
* You should see a new button `Go to Trace` next to the `trace_id` label,
* Click on the button.

👀 Grafana should open a new pane with the corresponding trace from Tempo!

We have added a new correlation dimension to our system between metrics and traces!

#### How was it done?

> aside positive
>
> This section is informative if you are interested in setting up such an integration in your Spring Boot application.

Regardless of the integration in Grafana and the setup in the Prometheus data source, we had to configure our application to make Micrometer expose exemplars with trace identifiers.

Firstly, we added the following dependencies to our application (`easypay-service/build.gradle.kts`), especially the `io.prometheus:prometheus-metrics-tracer-otel-agent` which provides the necessary classes:

```kotlin
dependencies {
  // ...
  implementation(platform("io.opentelemetry:opentelemetry-bom:1.38.0"))
  implementation("io.opentelemetry:opentelemetry-api")
  implementation("io.prometheus:prometheus-metrics-tracer-otel-agent:1.3.1")
}
```

Then, in our Spring Boot application, we added a new class, annotated with `@Configuration`, that provides an `OpenTelemetryAgentSpanContext` bean. This bean is able to retrieve the current trace ID from distributed tracing. Here is the implementation of `com.worldline.easypay.config.PrometheusRegistryConfiguration.java`:

```java
package com.worldline.easypay.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.prometheus.metrics.tracer.otel_agent.OpenTelemetryAgentSpanContext;

@Configuration // (1)
public class PrometheusRegistryConfiguration {
    
    @Bean
    @ConditionalOnClass(name="io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span") // (2)
    public OpenTelemetryAgentSpanContext exemplarConfigSupplier() {
        return new OpenTelemetryAgentSpanContext(); // (3)
    }
}
```
1. Declare the class as a configuration provider for Spring,
2. The bean injection is enabled only if the class is present (e.g., when the Java agent is attached). Otherwise, the application will not start due to missing classes,
3. Inject the `OpenTelemetryAgentSpanContext`, which will be used by the Micrometer Prometheus registry to export exemplars based on the trace ID.

> aside positive
>
> `OpenTelemetryAgentSpanContext` should be used when you are using an OpenTelemetry Java agent.
> If you plan to use OpenTelemetry directly in your application, you can rely on the `OpenTelemetrySpanContext` supplier provided by the `io.prometheus:prometheus-metrics-tracer-otel` dependency.

At this point, your application is ready to export exemplars with metrics once an OpenTelemetry Java agent is attached to the JVM!

### Traces to Logs

We are able to link logs to traces thanks to Loki’s data source `Derived fields`, but what about traces to logs?

Fortunately, the Tempo data source can be configured the same way!

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Tempo` data source,
* Configure `Trace to logs` as follows:
  * Data source: `Loki`,
  * Span start time shift: `-5m`,
  * Span end time shift: `5m`,
  * Tags: `service.name` as `application`,
  * Enable `Filter by trace ID` if and only if you want to show logs that match the trace ID.
* Click on `Save & test`.

ℹ️ Just to give you some context about this configuration:

* *Span start time shift* and *Span end time shift* allow retrieving logs within the specified interval, as log timestamps and trace timestamps may not match exactly.
* *Tags* is required: we should have a common tag between Tempo and Loki to bridge the gap between traces and logs. Here, the application name (defined as `service.name` in Tempo and `application` in Loki) serves this purpose.
* Using filters will remove logs that do not match trace or span identifiers.

#### Test correlation

🛠️ Hit the easypay payment endpoint with curl or k6 to generate some traces (whichever you prefer):

* `http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000`
* `k6 run -u 1 -d 2m k6/01-payment-only.js`

🛠️ Open the Grafana `Explore` dashboard to find a trace:
* You can use the following TraceQL directly: `{name="POST easypay-service"}`
  * It is equivalent to filtering on Span Name: `POST easypay-service`
  * More information about TraceQL: [Grafana TraceQL documentation](https://grafana.com/docs/tempo/latest/traceql/)
* Drill down a trace (you can widen the new pane).

👀 A new **LOG** icon should appear for each line of the trace (middle of the screen):

* Click on the icon for several spans (for the various services involved in the transaction),
* You can try toggling the `Filter by trace ID` option in the Tempo data source configuration to see the difference.

Yeah, we have added a new dimension to the correlation of our telemetry data, further improving our observability.

### Traces to Metrics

The last correlation we will explore in today’s workshop is between traces and metrics.

Sometimes, we are interested in knowing the state of our application when inspecting traces, such as JVM heap or system CPU usage.

In this section, we will configure the Tempo data source to link our traces to these metrics.

#### Enable correlation

🛠️ In Grafana, go to `Connections` > `Data sources`:

* Select the `Tempo` data source,
* Configure `Trace to metrics` as follows:
  * Data source: `Prometheus`,
  * Span start time shift: `-2m`,
  * Span end time shift: `2m`,
  * Tags: `service.name` as `application`

🛠️ Now, we will add some metric queries (click on `+ Add query` for each query):

* Heap usage as ratio:
  * Link Label: `Heap Usage (ratio)`,
  * Query: `sum(jvm_memory_used_bytes{$__tags})/sum(jvm_memory_max_bytes{$__tags})`
* System CPU usage:
  * Link Label: `System CPU Usage`,
  * Query: `system_cpu_usage{$__tags}`

> aside positive
>
> `$__tags` will be expanded by the tags defined in the `Tags` section.  
> For `easypay-service`, the query becomes `system_cpu_usage{application=easypay-service}`

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

