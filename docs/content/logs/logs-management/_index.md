+++
date = '2025-06-07T12:06:34+02:00'
title = 'Logs Management'
weight = 3
+++

> Let's dive into our logs with Grafana!

There are several ways to export logs to a log collector such as Loki or an ElasticSearch instance:

* Writing logs into a file and exporting its content with an external process (such as FileBeat or Promtail),
* Sending logs directly to the collector.

ℹ️ We target Loki as our log storage backend.

## Exporting content of a log file & Structured Logging

This approach consists in letting your logging framework write your logs into a file, which is read by another process
to send them to a log collector.

It is a common way to export logs. You just have to configure a log file processor such as FileBeat or Promtail to
read the file and send its content in the corresponding log backend.

It also requires to format your logs in a structured way, such as JSON, to ease its parsing and ingestion in the
backend.
This is known as Structured Logging.

With Spring Boot, you can write your logs in Elastic Common Schema (ECS), Graylog Extended Log Format (GELF) or Logstash
JSON formats:
[Structured Logging with Spring Boot Logging](https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured).

📝 **OPTIONAL:** You can try to change the console log format to one of ``ecs``, ``gelf`` or ``logstash`` in
``easypay-service``, by adding to the application.yaml file:

```yaml
# Structured Logging: could be either ecs, gelf or logstash
logging:
  # ...
  structured:
    format:
      console: ecs
``````

🛠️ Rebuild and redeploy the `easypay-service`:

```bash
$ docker compose up -d --build easypay-service
```

👀 Check logs in the console to see the new format.

```
(...)
easypay-service  | {"@timestamp":"2025-04-11T21:02:40.171457832Z","log.level":"INFO","process.pid":1,"process.thread.name":"http-nio-8080-exec-1","service.name":"easypay-service","log.logger":"org.apache.catalina.core.ContainerBase.[Tomcat].[localhost].[\/]","message":"Initializing Spring DispatcherServlet 'dispatcherServlet'","ecs.version":"8.11"}
easypay-service  | {"@timestamp":"2025-04-11T21:02:40.171819865Z","log.level":"INFO","process.pid":1,"process.thread.name":"http-nio-8080-exec-1","service.name":"easypay-service","log.logger":"org.springframework.web.servlet.DispatcherServlet","message":"Initializing Servlet 'dispatcherServlet'","ecs.version":"8.11"}
easypay-service  | {"@timestamp":"2025-04-11T21:02:40.175088520Z","log.level":"INFO","process.pid":1,"process.thread.name":"http-nio-8080-exec-1","service.name":"easypay-service","log.logger":"org.springframework.web.servlet.DispatcherServlet","message":"Completed initialization in 3 ms","ecs.version":"8.11"}
(...)
```

> [!IMPORTANT]
> Structured logging may be less readable for humans, but it is perfect for log concentrators as they are easy to parse!

## Our choice: sending logs directly to the log collector

It is also possible to send logs directly to a log collector by configuring an appender in your logging framework.

This approach offers a more real-time experience compared to the previous method.

Loki can ingest logs using its own API or using the OpenTelemetry protocol. So we have several options:

* Using the [Loki Logback appender](https://loki4j.github.io/loki-logback-appender/),
* Using
  the [OpenTelemetry Logback appender](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/library)
  provided by the OpenTelemetry project,
* Or even better, a zero code instrumentation approach with
  the [OpenTelemetry Java Agent](https://opentelemetry.io/docs/zero-code/java/agent/).

We will use the latter as we focus on what OpenTelemetry can bring to us for the observability of our Java applications
😉, and not only Spring Boot ones!

### 2 ways of instrumentation: Pros & Cons

There is two ways to instrument the byte code and broadcast telemetry : Using a library or through a Java Agent
Here is a short summary of the pros & cons

**Java Agent**

* It is the default choice for instrumenting a Java program
* Enable loose coupling between the the artifact & the agent

**Library / Starter**

* Faster than using an agent
* Mandatory with native mode

If you want to know more about this topic, you can [check out this documentation](https://opentelemetry.io/docs/zero-code/java/spring-boot-starter/).

### Target Architecture

![Logs Architecture](./archi-logs.png)

The OpenTelemetry Collector is an important component in our architecture: it acts as an ETL (Extract, Transform, Load)
process for
telemetry data (logs, metrics and traces).
It will receive logs from the application, transform them into a format that can be ingested by the log storage backend,
and send them to the backend.

A practice is to install a collector on each host where your application is running, or kube node.
It will then collect logs from all applications running on the host.

In the next steps, we will attach the OpenTelemetry Agent to the `easypay-service`, and configure it to send logs
to the OpenTelemetry Collector.

### OpenTelemetry Java Agent

The [OpenTelemetry Java Agent](https://opentelemetry.io/docs/zero-code/java/agent/) is a Java agent that can be attached
to a JVM to automatically instrument your application.

It is able to collect and send all your application telemetry: logs, metrics and traces, for most of the frameworks and
libraries you may use in your application.

👀 You can have a look to
[all the supported libraries, frameworks, applications servers and JVMs](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md)
supported by the Agent.

ℹ️ To attach an agent to a JVM, you just have to add the `-javaagent` option to the JVM command line.

ℹ️ `opentelemetry-javaagent.jar` is already available as `/opentelemetry-javaagent.jar` in the container (`easypay-service/src/main/docker/Dockerfile`):

```Dockerfile
ENV OTEL_AGENT_VERSION "v2.14.0"
ENV OTEL_AGENT_URL "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar"

ADD --chown=$UID:$GID ${OTEL_AGENT_URL} /opentelemetry-javaagent.jar
```

📝 Modify the `entrypoint` definition in the `compose.yml` file to attach the OpenTelemetry Java Agent to the
`easypay-service`:

```yaml
services:
  easypay-service:
    # ...
    entrypoint:
      - java
      - -javaagent:/opentelemetry-javaagent.jar # < Add this line 
      - -cp
      - app:app/lib/*
      - com.worldline.easypay.EasypayServiceApplication
```

By default, the OpenTelemetry Agent target endpoint is configured to `localhost:4317`.
It is overridable by setting the system property `otel.exporter.otlp.endpoint` or by using
the `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable.

ℹ️ Our collector is listening on `http://opentelemetry-collector:4317`.

📝 Add the following environment variables to the `easypay-service` service in the `compose.yml` file such as:

```yaml
services:
  easypay-service:
    # ...
    environment:
      # ...
      OTEL_RESOURCE_ATTRIBUTES: "service.name=easypay-service,deployment.environment=dev,service.namespace=service,service.version=1.0.0,service.instance.id=easypay-service:8080" # (1)
      OTEL_EXPORTER_OTLP_PROTOCOL: grpc # (2)
      OTEL_EXPORTER_OTLP_ENDPOINT: http://opentelemetry-collector:4317 # (3)
    # ...
```

1. The `OTEL_RESOURCE_ATTRIBUTES` environment variable is used to define the service name, the deployment environment,
   the
   service namespace, the service version and the service instance id,
    * OpenTelemetry’s [Open Agent Management Protocol specification](https://github.com/open-telemetry/opamp-spec/blob/main/specification.md)
    defines some [expected attributes](https://github.com/open-telemetry/opamp-spec/blob/main/specification.md#agentdescriptionidentifying_attributes)
    for telemetry data.
2. The `OTEL_EXPORTER_OTLP_PROTOCOL` environment variable is used to define the protocol used to send telemetry data to
   the collector.
3. The `OTEL_EXPORTER_OTLP_ENDPOINT` environment variable is used to define the endpoint of the collector.

### MDC support

By default, exporting MDC values with the OpenTelemetry Java Agent is experimental and requires an opt-in configuration.

But that does not prevent us from using it in our workshop! We should set the following properties:

* `otel.instrumentation.logback-appender.experimental-log-attributes=true`
* `otel.instrumentation.logback-appender.experimental.capture-mdc-attributes=*`
    * Wildcard means that we want all the MDC attributes.

Agent can be configured using either:

* System properties,
* Environment variables,
* Or configuration file.

📝 Modify the `entrypoint` definition in the `compose.yml` file to add the following system properties:

```yaml
services:
  easypay-service:
    # ...
    entrypoint:
      - java
      - -javaagent:/opentelemetry-javaagent.jar
      - -Dotel.instrumentation.logback-appender.experimental-log-attributes=true       # < Add this line
      - -Dotel.instrumentation.logback-appender.experimental.capture-mdc-attributes=*  # < Add this line
      - -cp
      - app:app/lib/*
      - com.worldline.easypay.EasypayServiceApplication
```

### OpenTelemetry Collector

> [!TIP]
> Utilizing collectors offers several advantages for managing telemetry data:
> - Reduces the need for complicated application configurations: just send data to `localhost`,
> - Centralizes configuration to a single point: the collector,
> - Acts as a buffer to prevent resource overuse,
> - Can transform data before ingestion,
> - Supports data intake from various protocols and can relay them to any backend,
> - ...

ℹ️ The OpenTelemetry collector is already configured to receive logs and forward metrics to the Loki backend.

👀 You can check the collector configuration located in the `docker/otelcol/otelcol.yaml` file.

```yaml
receivers:
  # Listen for telemetry data via OpenTelemetry protocol
  otlp: # (1)
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch: # (2)

exporters:
  # Configure an exporter using the OpenTelemetry protocol over HTTP to send logs to Loki
  otlphttp/loki: # (3)
    endpoint: http://loki:3100/otlp

service:
  pipelines:
    # Declare the pipeline for logs processing:
    # 1. Receive logs via the OpenTelemetry protocol
    # 2. Optimize data by batching them (optional but recommended)
    # 3. Export logs to Loki
    logs: # (4)
      receivers: [ otlp ]
      processors: [ batch ]
      exporters: [ otlphttp/loki ]
```

1. Declare an input receiver to listen for telemetry data via the OpenTelemetry protocol on ports 4317 (GRPC) and 4318 (HTTP/PROTOBUF),
2. Declare a processor of type batch to optimize data by batching them,
3. Declare an exporter to send logs to Loki using the OpenTelemetry protocol over HTTP,
  * Exporter name here contains its type (otlphttp) and a key (loki),
  * The key is optional, and is used to differentiate exporters of the same type,
  * You should use the full exporter name in the pipeline configuration.
4. Configure the logs pipeline by defining the receivers, processors, and exporters to use.

🛠️ Redeploy the `easypay-service`:

```bash
$ docker compose up -d easypay-service
```

✅ To ensure `easypay-service` has started up correctly, check its logs with:

```bash
$ docker compose logs -f easypay-service
```

✅ If Java agent was correctly taken into account, logs should start with:

```
easypay-service  | OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
easypay-service  | [otel.javaagent 2025-01-16 15:41:46:550 +0000] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: 2.11.0
```

## Explore logs with Grafana

> aside positive
>
> For this workshop, we have already configured the Loki datasource in Grafana.  
> You can take a look at its configuration in Grafana (port `3000`) by navigating to the `Connections` > `Data sources`
> section.  
> We only set up the Loki server url.

🛠️ Go to Grafana (port `3000`):

* Open an `Explore` dashboard,
* Select the Loki datasource.

Grafana offers a form to help you build your queries:

* Filtering labels,
* Finding text in logs,
* Parsing logs to extract and filter values,
* ...

You can also use a dedicated query language to make your queries directly: this is
named [LogQL](https://grafana.com/docs/loki/latest/query/).

🛠️ Let’s get logs from the `easypay-service`:

* In the `Label filter`, select the application with ``service_name`` equal to ``easypay-service``,
* Click on ``Run Query``,
* Check out logs on the bottom of the view and unfold some of them.

🛠️ Do not hesitate to hit the easypay payment endpoint with curl/httpie or k6 to generate some logs (whichever you
prefer):

```bash
http POST :8080/api/easypay/payments posId=POS-01 cardNumber=5555567898780008 expiryDate=789456123 amount:=40000
# OR
k6 run -u 1 -d 1m k6/01-payment-only.js
```

👀 You can also view logs for the other services (e.g., ``api-gateway``).

Maybe another issue? Do you see the card numbers? 😨

## Personal Identifiable Information (PII) obfuscation

For compliance and to prevent personal data loss, we will obfuscate the card number in the logs.

The OpenTelemetry collector in its contrib flavor provides
a [redaction processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/redactionprocessor)
we can use to obfuscate sensitive data.
The processor can be declared and attached to the log pipeline in order to mask all attributes containing a sensitive
value.

📝 Let’s add the redaction processor to the OpenTelemetry collector configuration:

```
(...)

processors:
  batch:

  redaction/card-numbers: # (1)
    allow_all_keys: true
    blocked_values:
      - "4[0-9]{12}(?:[0-9]{3})?" ## VISA
      - "(5[1-5][0-9]{14}|2(22[1-9][0-9]{12}|2[3-9][0-9]{13}|[3-6][0-9]{14}|7[0-1][0-9]{13}|720[0-9]{12}))" ## MasterCard
      - "3(?:0[0-5]|[68][0-9])[0-9]{11}" ## Diners Club
      - "3[47][0-9]{13}" ## American Express
      - "65[4-9][0-9]{13}|64[4-9][0-9]{13}|6011[0-9]{12}|(622(?:12[6-9]|1[3-9][0-9]|[2-8][0-9][0-9]|9[01][0-9]|92[0-5])[0-9]{10})" ## Discover
      - "(?:2131|1800|35[0-9]{3})[0-9]{11}" ## JCB
      - "62[0-9]{14,17}" ## UnionPay
    summary: debug

(...)

service:
  pipelines:
    logs:
      receivers: [otlp]
      processors: [batch,redaction/card-numbers] # (2)
      exporters: [otlphttp/loki]

(...)
```

1. We declare a new processor named `redaction/card-numbers` that will obfuscate all attributes containing a card
   number,
2. We attach the processor to the logs pipeline.

🛠️ Restart the collector to take into account the new configuration:

```bash
docker compose up -d --build opentelemetry-collector
```

🛠️ Generate some logs with curl/httpie or k6.

✅ Check the card numbers are now obfuscated with the ``****`` content.

> aside positive
>
> Having a collector located near to your application provides several benefits:
> * It reduces latency between the application and the collector,
> * You can have a collector configuration tailored to your application needs (here by redacting sensitive data).