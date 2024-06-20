**author: David Pequegnot & Alexandre Touret
summary: Observability
id: observability-workshop
categories: observability
environments: Web
status: Published
feedback link: 

# Make your Java application fully observable with the Grafana Stack

## Introduction

This workshop aims to introduce how to make a Java application fully observable with:
* Proper logs with insightful information
* Metrics with [Prometheus](https://prometheus.io/)
* [Distributed Tracing](https://blog.touret.info/2023/09/05/distributed-tracing-opentelemetry-camel-artemis/)

During this workshop we will use the Grafana stack and Prometheus:

* [Grafana](https://grafana.com/): for dashboards
* [Loki](https://grafana.com/oss/loki/): for storing our logs
* [Tempo](https://grafana.com/oss/tempo/): for storing traces
* [Prometheus](https://prometheus.io/): for gathering and storing metrics.

We will also cover the OpenTelemetry Collector which gathers & broadcast then the data coming from our microservices

## Workshop overview
Duration: 0:02:00

### Application High Level Design

![The Easy Pay System](./img/architecture.svg)

#### API Gateway
Centralises all API calls.

#### Easy Pay Service
Payment microservices which accepts (or not) payments.

This is how it validates every payment:
1. Check the POS number
2. Check the credit card number
3. Check the credit card type
4. Check the payment threshold, it calls the Smart Bank Gateway for authorization

If the payment is validated it stores it and broadcasts it to all the other microservices through Kafka.

#### Fraud detection Service

After fetching a message from the Kafka topic, this service search in its database if the payment's card number is registered for fraud.

In this case, only a WARN log is thrown.

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
* The [OTEL collector](https://opentelemetry.io/docs/collector/) which gathers all the data to send it then to 

In addition, the microservices are started with an agent to broadcast the traces to the collector.   

## Prerequisites
### Skills

| Skill                                                                                                                                                                                                                                                                                   | Level        | 
|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| [REST API](https://google.aip.dev/general)                                                                                                                                                                                                                                              | proficient   |
| [Java](https://www.oracle.com/java/)                                                                                                                                                                                                                                                    | novice       |   
| [Gradle](https://gradle.org/)                                                                                                                                                                                                                                                           | novice       |
| [Spring Framework](https://spring.io/projects/spring-framework), [Boot](https://spring.io/projects/spring-boot), [Cloud Config](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_quick_start), [Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) | proficient |
| [Docker](https://docs.docker.com/)                                                                                                                                                                                                                                                      | novice       |
| [Grafana stack](https://grafana.com/)                                                                                                                                                                                                                                                   | novice       |
| [Prometheus](https://prometheus.io/)                                                                                                                                                                                                                                                    | novice       |
| [Kafka](https://kafka.apache.org/)                                                                                                                                                                                                                                                      | novice       |

### Tools
#### If you want to execute this workshop locally
You **MUST** have set up these tools first:
* [Java 21+](https://adoptium.net/temurin/releases/?version=21)
* [Gradle 8.7+](https://gradle.org/)
* [Docker](https://docs.docker.com/) & [Docker compose](https://docs.docker.com/compose/)
* Any IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea), [VSCode](https://code.visualstudio.com/), [Netbeans](https://netbeans.apache.org/),...) you want
* [cURL](https://curl.se/), [jq](https://stedolan.github.io/jq/), [HTTPie](https://httpie.io/) or any tool to call your REST APIs

Here are commands to validate your environment:

**Java**

```jshelllanguage
$ java -version

openjdk version "21.0.3" 2024-04-16 LTS
OpenJDK Runtime Environment Temurin-21.0.3+9 (build 21.0.3+9-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.3+9 (build 21.0.3+9-LTS, mixed mode, sharing)
```

**Gradle**

If you use the wrapper, you won't have troubles. Otherwise...:

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

##### With Gitpod (recommended)
You can use [Gitpod](https://gitpod.io).
You must create an account first.
You then can open this project in either your local VS Code or directly in your browser:

[![Open in Gitpod](img/open-in-gitpod.svg)](https://github.com/worldline/observability-workshop/observability-workshop.git)

## Environment Setup
Duration: 0:05:00

### Open GitPod

We will assume you will use GitPod for this workshop :) 

[![Open in Gitpod](img/open-in-gitpod.svg)](https://gitpod.io/#github.com/worldline/observability-workshop.git)

### Start the infrastructure

The "infrastructure stack" is composed of the following components:
* One [PostgreSQL](https://www.postgresql.org/) instance per micro service
* One [Kafka broker](https://kafka.apache.org/)
* One [Service Discovery](https://spring.io/guides/gs/service-registration-and-discovery) microservice to enable load balancing & loose coupling.
* One [Configuration server](https://docs.spring.io/spring-cloud-config/) is also used to centralise the configuration of our microservices.
* The following microservices: API Gateway, Merchant BO, Fraud Detect, Smart Bank Gateway

To run it, execute the following command

``` bash
$ docker compose up -d --build --remove-orphans
```
To check if all the services are up, you can run this command:

``` bash
$ docker compose ps -a
```
And check the status of every service.
        
### Start the rest of our microservices
        
You can now start the application with the following commands.
For each you must start a new terminal in VSCode.

#### The REST Easy Pay Service
Run the following command:

```bash
$ ./gradlew :easypay-service:bootRun -x test
```

#### Validation

Open the Eureka website started during the infrastructure setup

If you run this workshop on your desktop, you can go to this URL: http://localhost:8761.
If you run it on GitPod, you can go to the corresponding URL (e.g., https://8761-worldline-observability-w98vrd59k5h.ws-eu114.gitpod.io) instead.

You can now reach our platform to initiate a payment:

```bash
$ http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=25000
```

You should get the following content:

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
Duration: 0:30:00

### Some functional issues
One of our customers raised an issue: 

> When I reach your API, I usually either an ``AMOUNT_EXCEEDED`` or ``INVALID_CARD_NUMBER`` error.

Normally the first thing to do is checking the logs. 
Before that, we will reproduce these issues.

You can check the API as following:

For the ``AMOUNT_EXCEEDED`` error:

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

And for the ``INVALID_CARD_NUMBER`` error:

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

Go then to the log folder (``easypay-service/logs/``) , look around the log files and look into these issues.

You should get these log entries in JSON format. Open one of these files and check out the logs.

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

The log level is a fundamental concept in logging, no matter which logging framework you use. It allows you to tag log records according to their severity or importance. SLF4J offers the following log levels by default:

* ``TRACE`` : typically used to provide detailed diagnostic information that can be used for troubleshooting and debugging. Compare to DEBUG messages, TRACE messages are more fine-grained and verbose.
* ``DEBUG``: used to provide information that can be used to diagnose issues especially those related to program state.
* ``INFO``: used to record events that indicate that program is functioning normally.
* ``WARN``: used to record potential issues in your application. They may not be critical but should be investigated.
* ``ERROR``: records unexpected errors that occur during the operation of your application. In most cases, the error should be addressed as soon as possible to prevent further problems or outages.


#### ``AMOUNT_EXCEEDED`` issue

Go the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/bank/BankAuthorService.java`` class and modify the following code block
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

Modify the exception trace to provide contextual information such as the authorId, the status of the call and the result. 

#### ``INVALID_CARD_NUMBER`` issue
Go to the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/CardValidator.java`` class and modify the following block code in the process method in the same way:

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
* The error message


You can also add more logs:
In the ``CarValidator.checkLunKey()`` method, you can add a warn message when the key is not valid. 
For instance:

```java
log.warn("checkLunKey KO: {}",cardNumber);
```
In the ``CarValidator.checkExpiryDate()`` method, you can add a warn message when a ``DateTimeParseException`` is thrown.
For instance:

```java
log.warn("checkExpiryDate KO: bad format {}",expiryDate);
```

You can go further and add as many log you think it would help in production.

### Check your code

You can restart your easy pay service by typing ``CTRL+C`` in your console prompt, and run the following command:

```bash
$ ./gradlew :easypay-service:bootRun -x test
```

Now you can run the same commands ran earlier and check again the logs.

### A technical issue

Another issue was raised for the POS (Point of Sell) ``POS-02``. 
When you reach the API using this command:

```bash
http POST :8080/api/easypay/payments posId=POS-02 cardNumber=5555567898780008 expiryDate=789456123 amount:=25000
```

With the following output:

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

You then get the following log message:

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

To find the root cause, add first a _smart_ log entry in the ``easypay-service/src/main/java/com/worldline/easypay/payment/control/PosValidator.java`` class.

In the ``isActive()`` method, catch the exception and trace the error:
> aside negative
>
> TODO Commande pour le message d'erreur + message d'erreur
>

You can also prevent this issue by simply fixing the SQL import file

In the file ``easypay-service/src/main/resources/db/postgresql/data.sql``, Modify the implied line for ``POS-02`` from:

```sql 
INSERT INTO pos_ref(id, pos_id, location, active) VALUES (2, 'POS-02', 'Blois France', NULL) ON CONFLICT DO NOTHING;
```

to 

```sql 
INSERT INTO pos_ref(id, pos_id, location, active) VALUES (2, 'POS-02', 'Blois France', true) ON CONFLICT DO NOTHING;
```

### Using Mapped Diagnostic Context (MDC) to get more insights
> aside positive
>
>  Mapped Diagnostic Context (MDC) will help us add more context on every log output. For more information, refer to this web page: https://logback.qos.ch/manual/mdc.html 


Go to the ``PaymentResource`` class and modify the method ``processPayment()`` to instantiate the [MDC](https://logback.qos.ch/manual/mdc.html):


```java
public ResponseEntity<PaymentResponse> processPayment(PaymentRequest paymentRequest)

MDC.put("context",paymentRequest);
[...]
MDC.clear();
return httpResponse;

```

Go to the MDC spring profile configuration file (``easypay-service/src/main/resources/application-mdc.properties``) and check the configuration got the ``context`` field.

Restart the application activating the ``mdc`` profile and see how the logs look like now.

```bash
./gradlew :easypay-service:bootRun -x test  --args='--spring.profiles.active=default,mdc'
```

> aside negative
>
> TODO Mettre comment vérifier que le bon profil a été démarré

### Adding more content in our logs

To have more logs, we will run several HTTP requests using [K6](https://k6.io/):

Run the following command:

```bash
$ k6 run -u 5 -d 5s k6/01-payment-only.js
```

Check then the logs to pinpoint some exceptions

### Logs Correlation  
> aside positive
>
> You are probably wondering how to smartly debug in production when you have plenty of logs for several users and by the way different transactions?
>
> One approach would be to correlate all of your logs using a correlation Id.
> If an incoming request has no correlation id header, the API creates it. If there is one, it uses it instead.

> aside negative
>
> TODO mettre la manipulation pour le correlation ID et un exemple d'utilisation

### Let's dive into our logs on Grafana!

Logs are stored in the logs folder (``easypay-service/logs``).

We use then [Promtail to broadcast them to Loki](https://grafana.com/grafana/dashboards/14055-loki-stack-monitoring-promtail-loki/) through [Grafana Alloy (OTEL Collector)](https://grafana.com/docs/alloy/latest/).

Check out the Logging configuration in the ``docker/alloy/config.alloy`` file:

> aside negative
>
> TODO mettre à jour

```json
////////////////////
// LOGS
////////////////////

// CLASSIC LOGS FILES
local.file_match "logs" {
	path_targets = [{"__path__" = "/logs/*.log", "exporter" = "LOGFILE"}]
}

loki.source.file "logfiles" {
	targets    = local.file_match.logs.targets
	forward_to = [loki.write.endpoint.receiver]
}

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

	//stage.luhn { }
    // (4)
	stage.json {
		expressions = {
			// timestamp   = "timestamp",
			application = "context.properties.applicationName",
			instance    = "context.properties.instance",
		}
	}
// (5)
	stage.labels {
		values = {
			application = "application",
			instance    = "instance",
		}
	}

	/*stage.timestamp {
		source = "timestamp"
		format = "RFC3339"
		fallback_formats = ["UnixMs",]
	}*/
}
    // (6)
// EXPORTER (LOKI)
loki.write "endpoint" {
	endpoint {
		url = "http://loki:3100/loki/api/v1/push"
	}
}
```
As you can see, the JSON files are automatically grabbed and broadcast to Loki.
Here is a short summary of the following steps:

1. Configuration of the input files
2. Configuration of the broadcasting within Alloy
3. Process definition
4. Applying some contextual information suck like the application name
5. Follow up the previous action and applying labels
6. Output definition

Open a browser page to Grafana.

Check out first the page ``http://localhost:12345/graph``
Select the ``loki.source.jsonlogfiles`` component.

Check all the targets.

Now open the explore dashboard : ``http://localhost:12345/explore``.

Select the Loki datasource.

In the label filter, select the application as ``easypay-service`` and click on ``Run Query``.


Add then a JSON parser operation , click on ``Run query`` again and check out the logs.

Additionally, you can add these expressions in the JSON parser operation box:

* Expression: ``message="message"``
* Level: ``level="level"``

Check out the logs again, view it now as a table.

You can also view traces for the other services (e.g., ``api-gateway``) 

Finally, you can search logs absed on the correlation ID

> aside negative
>
> TODO MEttre la procédure


## Metrics
Duration: 0:30:00

Check out the ``easypay-service`` metrics definitions first:

```bash
http :8080/actuator/metrics
```

Explore the output

Now get the prometheus metrics using this command:

```bash
http :8080/actuator/metrics
```

You can also have an overview of all the prometheus endpoints metrics on the Prometheus dashboad . 

Go to ``http://localhost:9090`` and explore the different endpoints in ``eureka-discovery``.

Go then to Grafana and start again a ``explore`` dashboard.

Select the ``Prometheus`` datasource.
You can for instance run this query: ``system_load_average_1m{application="api-gateway"}``

Click on ``Run Query`` button.

Now let's add more content using K6.

Run the following command: 

```bash
$ k6 run -u 5 -d 2m k6/02-payment-smartbank.js
```

Go back to the Grafana dashboard, click on ``Dashboards`` and select ``JVM Micrometer``.

Explore the dashboard, especially the Garbage collector and CPU statistics.

Look around the JDBC dashboard then and see what happens on the database connection pool.


> aside negative
>
> TODO Détailler

Now, let's go back to the Loki explore dashboard and see what happens:

Create a query with the following parameters:

* Label filters: ``application`` = ``smartbank-gateway``
* line contains/Json: ``expression``= ``level=level``
* label filter expression: ``label`` = ``level ; ``operator`` = ``!=`` ; ``value`` = ``INFO`` 

Click on ``Run query`` and check out the logs.

Normally you would get a ``java.lang.OutOfMemoryError`` due to a saturated Java heap space.


To get additional insights, you can go back to the JVM dashboard and select the ``smartbank-gateway`` application.

Normally you will see the used JVM Heap reaching the maximum allowed.

## Traces
Duration: 0:20:00

Stop the easypay service.

Open the ``easypay.sh`` script file. You will then how is configured the JVM startup with  the ``-javaagent`` parameter.

```shell
#!/usr/bin/env bash

export OTEL_SERVICE_NAME=easypay-service
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_RESOURCE_ATTRIBUTES="source=agent"

export SERVER_PORT=8081
export LOGS_DIRECTORY="$(pwd)/logs"

java -Xms512m -Xmx512m -javaagent:$(pwd)/instrumentation/grafana-opentelemetry-java.jar -jar "$(pwd)/easypay-service/build/libs/easypay-service-0.0.1-SNAPSHOT.jar" "$@"
```

During this workshop, we will use an OpenTelemetry agent for broadcasting traces through Alloy to Tempo.  

Check the environment variables used:

* ``OTEL_SERVICE_NAME``
* ``OTEL_EXPORT_OTLP_ENDPOINT``
* ``OTEL_EXPORT_OTLP_PROTOCOL``
* ``OTEL_EXPORT_ATTRIBUTES``

Now open a new explore Grafana dashboard.

Select the Tempo datasource.

Look around the node graph, pinpoint what are the different nodes and corresponding response times.

Create a query, select service name as ``easypay-service``.

Click on ``Run query`` and Drill down a Trace ID to get the full stack of the corresponding transaction. 

Explore the corresponding SQL queries and their response times.

Finally, check the traces from different services (e.g., ``api-gateway``).

## Correlate Traces, Logs
Duration: 0:15:00

## Performance testing: ALL IN!!
Duration: 0:15:00
