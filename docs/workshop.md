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
Duration: 0:02:00

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

If the payment is validated it stores it and broadcasts it to all the other microservices through Kafka.

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
* The [OTEL collector](https://opentelemetry.io/docs/collector/) which gathers all the data to send it then to 

In addition, the microservices are started with an agent to broadcast the traces to the collector.   

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

[![Open in Gitpod](img/open-in-gitpod.svg)](https://gitpod.io/#github.com/worldline/observability-workshop.git)

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

If you run your application on GitPod, the following step is automatically started at the startup.

Otherwise, to run it on your desktop, execute the following commands

``` bash
$ bash scripts/download-agent.sh
```

``` bash
$ ./gradlew tasks
```

``` bash
$ docker compose up -d --build --remove-orphans
```
To check if all the services are up, you can run this command:

``` bash
$ docker compose ps -a
```
And check the status of every service.

#### Validation

Open the [Eureka](https://cloud.spring.io/spring-cloud-netflix/) website started during the infrastructure setup.

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
        
You can also prevent this issue by simply fixing the SQL import file.

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

> aside positive
>
> You can verify the MDC profile is applied by checking the presence of this log message:
> ``The following 2 profiles are active: "default", "mdc"``
> 

### Adding more content in our logs

To have more logs, we will run several HTTP requests using [K6](https://k6.io/):

Run the following command:

```bash
$ k6 run -u 5 -d 5s k6/01-payment-only.js
```

Check then the logs to pinpoint some exceptions.

### Personal Identifiable Information (PII)  bfuscation
For compliance and preventing personal data loss, we will obfuscate the card number in the logs:

In the Alloy configuration file (``docker/alloy/config.alloy``), add the [luhn stage](https://grafana.com/docs/alloy/latest/reference/components/loki.process/#stageluhn-block) into the ``jsonlogs`` loki process stage

``
stage.luhn {
replacement= "**DELETED**"
}
``

We will then have the following configuration for processing the JSON logs:

```
loki.process "jsonlogs" {
	forward_to = [loki.write.endpoint.receiver]

	stage.luhn {
    	    replacement= "**DELETED**"
    	}

	stage.json {
		expressions = {
			// timestamp   = "timestamp",
			application = "context.properties.applicationName",
			instance    = "context.properties.instance",
			trace_id    = "mdc.trace_id",
		}
	}

	stage.labels {
		values = {
			application = "application",
			instance    = "instance",
			trace_id    = "trace_id",
		}
	}

}

```


Restart then Alloy: 

```bash
$ docker restart collector
```
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

```json
////////////////////
// (1) LOGS
////////////////////

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

Finally, you can search logs based on the correlation ID

> aside negative
>
> TODO MEttre la procédure


## Metrics
Duration: 0:30:00

Let’s take control of our application’s metrics!

> aside positive
>
> EasyPay is already configured to expose metrics to Prometheus format with 
> [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)
> and [Micrometer](https://micrometer.io/).

### Metrics exposed by the application

Check out the ``easypay-service`` metrics definitions exposed by Spring Boot Actuator first:

```bash
http :8080/actuator/metrics
```

Explore the output.

Now get the Prometheus metrics using this command:

```bash
http :8080/actuator/prometheus
```

This is an endpoint exposed by Actuator to let the Prometheus server get your application metrics.

### How are metrics scraped?

Check out the Prometheus (``docker/prometheus/prometheus.yml``) configuration file.
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

You can have an overview of all the scraped applications on the Prometheus dashboard:

* Go to ``http://localhost:9090`` if you started the stack locally, or use the link provided by GitPod in the `PORTS` view for port `9090`,
* Click on ``Status`` > ``Targets``,
* Explore the different services discovered in the ``eureka-discovery`` section:
  * You should not see the ``easypay-service``...
  * ... but rest assured, we will fix that!

### Add scrape configuration for our easypay service

Modify the ``docker/prometheus/prometheus.yml`` file to add a new configuration to scrape the easypay service.
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

Restart the Prometheus server to take into account this new configuration:

```bash
docker compose restart prometheus
```

Now explore again the targets (``Status`` > ``Targets``) on the Prometheus dashboard (``port 9090``). 

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

* Go to Grafana and start again an ``Explore`` dashboard.

* Select the ``Prometheus`` datasource instead of the ``Loki`` one.

In this section you will hands on the metrics query builder of Grafana.

The ``Metric`` field lists all the metrics available in Prometheus server: take time to explore them.

* For example, you can select the metric named ``jvm_memory_used_bytes``, and click on the ``Run query`` button to plot the memory usage of all your services by memory area,

* If you want to plot the total memory usage of your services:
  * Click on ``Operations`` and select ``Aggregations`` > ``Sum``, and ``Run query``: you obtain the whole memory consumption of all your JVMs,
  * To split the memory usage per service, you can click on the ``By label`` button and select the label named ``application`` (do not forget to click on ``Run query`` afterthat).

* You can also filter metrics to be displayed using ``Label filters``: try to create a filter to display only the metric related to the application named easypay-service.

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

To import these dashboards:
* Go to Grafana (``port 3000``), and select the ``Dashboards`` section on the left,
* Then click on ``New`` (top right), and click on ``Import``,
* In the ``Find and import…`` field, just paste the ID of the dashboard and click on ``Load``,
* In the ``Select a Prometheus data source``, select ``Prometheus`` and click on ``Import``,
* You should be redirected to the newly imported dashboard.

> aside positive
>
> Imported dashboards are available directly from the ``Dashboards`` section of Grafana.

Explore the ``JVM Micrometer`` dashboard: it works almost out of box.  
It contains lot of useful information about JVMs running our services.

The ``application`` filter (top of the dashboard) let you select the service you want to explore metrics.

### Incident!

Now let's simulate some traffic using Grafana K6.

Run the following command: 

```bash
$ k6 run -u 5 -d 2m k6/02-payment-smartbank.js
```

Go back to the Grafana dashboard, click on ``Dashboards`` and select ``JVM Micrometer``.

Explore the dashboard for the ``easypay-service``, especially the Garbage collector and CPU statistics.

Look around the other ``Spring Boot JDBC & HikariCP`` dashboard then and see what happens on the database connection pool for ``easypay-service``.

We were talking about an incident, isn’t it? Let's go back to the Explore view of Grafana, select Loki as a data source and see what happens!

Create a query with the following parameters to get error logs of the ``smartbank-gateway`` service:

* Label filters: ``application`` = ``smartbank-gateway``
* line contains/Json: ``expression``= ``level="level"``
* label filter expression: ``label`` = ``level ; ``operator`` = ``=~`` ; ``value`` = ``WARN|ERROR`` 

Click on ``Run query`` and check out the logs.

Normally you would get a ``java.lang.OutOfMemoryError`` due to a saturated Java heap space.

To get additional insights, you can go back to the JVM dashboard and select the ``smartbank-gateway`` application.

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

Let’s modify the ``com.worldline.easypay.payment.control.PaymentService`` class to declare them:

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
3. Intitialize the two timers by giving them a name (4), a description (5) and adding them to the meter registry.

#### 2. Record time spent in the methods

The ``Timer`` API [allows to record blocks of code](https://docs.micrometer.io/micrometer/reference/concepts/timers.html#_recording_blocks_of_code). It’s going to be our way:

```java
timer.record(() -> {
  // some code
});
```

Let’s modify our `process` and `store` methods to record our latency with the new metrics.  
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

Let’s do the same for the counter:

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

The method ``accept`` of the ``PaymentService`` class is invoked for each payment request, it is a good candidate to increment our counter:

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

Rebuild the easypay-service:

```bash
docker compose build easypay-service
```

Redeploy easypay:

```bash
docker compose up -d easypay-service
```

Once easypay is started (you can check logs with the ``docker compose logs -f easypay-service`` command and wait for an output like ``Started EasypayServiceApplication in 32.271 seconds``):

* Execute some queries: 

```bash
http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
```

* Go into the container and query the ``actuator/prometheus`` endpoint to look at our new metrics:

```bash
docker compose exec -it easypay-service sh
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

Modify the two timers as follows:

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

Repeat the previous step `3. Redeploy easypay`.

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

Each ` bucket ` contains the number of event which lasts less than the value defined in the ``le`` tag.

#### 6. Visualization

Go back to Grafana (`port 3000`), and go into the ``Dashboards`` section.

We will import the dashboard defined in the ``docker/grafana/dashboards/easypay-monitoring.json`` file:
* Click on ``New`` (top right), and select ``Import``,
* In the ``Import via dashboard JSON model`` field, paste the content of the ``easypay-monitoring.json``  file and click on ``Load``,
* Select Prometheus as a data source.

You should be redirected to the ``Easypay Monitoring`` dashboard.

It provides some dashboards we have created from the new metrics you exposed in your application:

* `Payment request count total (rated)`: represents the number of hit per second in our application computed from our counter,
* ``Payment Duration distribution``: represents the various percentiles of our application computed from the ``rivieradev_payment_process`` timer and its histogram,
* ``Requests process performance`` and ``Requests store performance``: are a visualization of the buckets of the two timers we created previously.

You can generate some load to view your dashboards evolving live:

```bash
k6 -u 2 -d 2m k6/01-payment-only.js
```

> aside positive
>
> Do not hesitate to explore the way the panels are created, and the queries we used!  
> Just hover the panel you are interested in, click on the three dots and select Edit.

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

### Sampling

To avoid storing useless data into Tempo, we can sample the data in two ways:
* [Head Sampling](https://opentelemetry.io/docs/concepts/sampling/#head-sampling)
* [Tail Sampling](https://opentelemetry.io/docs/concepts/sampling/#head-sampling)

In this workshop, we will implement the latter.

In the alloy configuration file (``docker/alloy/config.alloy``), put this configuration just after the ``SAMPLING`` comment:
```
// SAMPLING
//
otelcol.processor.tail_sampling "actuator" {
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
```

This configuration will filter the [SPANs](https://opentelemetry.io/docs/concepts/signals/traces/#spans) created from ``/actuator`` API calls.

Restart then Alloy.

```bash
$ docker compose restart collector
```

## Correlate Traces, Logs
Duration: 0:15:00


Let's go back to the Grafana explore dashboard. 
Select the ``Loki`` datasource
As a label filter, select ``easypay-service``
Run a query and select a log entry.

Now check you have a ``mdc`` JSON element which includes both [``trace_id``](https://www.w3.org/TR/trace-context/#trace-id) and [``span_id``](https://www.w3.org/TR/trace-context/#parent-id).
They will help us correlate our different requests logs and traces.

> aside positive
>
> These notions are part of the [W3C Trace Context Specification](https://www.w3.org/TR/trace-context/).

Now, go below in the Fields section. 
You should see a ``Links`` sub-section with a ``View Trace`` button.

Click on it.
You will see the corresponding trace of this log.

Now you can correlate logs and metrics!
If you have any exceptions in your error logs, you can now check out where it happens and see the big picture of the transaction (as a customer point of view).

### How was it done?

When you enable the MDC on your logs, you always have filled the ``trace_id``.

Then to enable the link, we added the following configuration into the Alloy configuration file:

```yaml
stage.json { (1)
		expressions = {
			// timestamp   = "timestamp",
			application = "context.properties.applicationName",
			instance    = "context.properties.instance",
			trace_id    = "mdc.trace_id",
		}
	}

	stage.labels { (2)
		values = {
			application = "application",
			instance    = "instance",
			trace_id    = "trace_id",
		}
	}
```

1. The first step extracts from the JSON file the ``trace_id`` field.
2. The label is then created to be eventually used on a Grafana dashboard.
3. _Et voila!_

