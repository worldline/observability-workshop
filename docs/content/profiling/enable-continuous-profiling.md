+++
date = '2025-06-07T16:24:31+02:00'
title = 'Enable Continuous Profiling'
weight = 2
+++

## Start the Pyroscope server

🛠️ We will use the `grafana/pyroscope` container image: we already defined a pyroscope service in our `compose.yml`
file, but it is not yet enabled. You can start it by enabling the `profiling` profile:

```bash
$ docker compose --profile=profiling up -d
```

✅ It should start a new service on port `4040`.

🛠️ Go to Grafana Pyroscope dashboard UI on port `4040`:

* You should see Pyroscope self-profiling and a new graph type: a flamegraph.
* On the top of the dashboard you can select the type of information you want to display:
    * CPU profiling,
    * Memory,
    * Goroutines (it’s a Go process),
    * Etc.
* You can also filter your data by tags…
* And there is a query language: you should be used to this by now! 😉

## Setup easypay-service for Continuous Profiling

Let’s use an agent again to profile our application, and the Pyroscope extension for OpenTelemetry agent
to match span with profiling data. 

✅ Both `pyroscope.jar` agent and `pyroscope-otel.jar` were downloaded for you in the `/` of the container (`easypay-service/src/main/docker/Dockerfile`):

```Dockerfile
ENV PYROSCOPE_VERSION=v2.0.0
ENV PYROSCOPE_URL="https://github.com/grafana/pyroscope-java/releases/download/${PYROSCOPE_VERSION}/pyroscope.jar"
ENV PYROSCOPE_OTEL_VERSION=v1.0.1
ENV PYROSCOPE_OTEL_URL="https://github.com/grafana/otel-profiling-java/releases/download/${PYROSCOPE_OTEL_VERSION}/pyroscope-otel.jar"

ADD --chown=$UID:$GID ${PYROSCOPE_URL} /pyroscope.jar
ADD --chown=$UID:$GID ${PYROSCOPE_OTEL_URL} /pyroscope-otel.jar
```

📝 Just like for logs and metrics, we should modify the `compose.yml` deployment file for the `easypay-service` to enable
and configure profiling with Pyroscope:

```yaml
services:
  easypay-service:
    # ...
    environment:
      # ...
      # Pyroscope agent configuration --vv
      PYROSCOPE_APPLICATION_NAME: easypay-service # (1)
      PYROSCOPE_FORMAT: jfr                       # (2)
      PYROSCOPE_PROFILING_INTERVAL: 10ms    
      PYROSCOPE_PROFILER_EVENT: itimer            # (3)
      PYROSCOPE_PROFILER_LOCK: 10ms               # (4)
      PYROSCOPE_PROFILER_ALLOC: 512k              # (5)
      PYROSCOPE_UPLOAD_INTERVAL: 5s
      OTEL_JAVAAGENT_EXTENSIONS: /pyroscope-otel.jar # (6)
      OTEL_PYROSCOPE_ADD_PROFILE_URL: false
      OTEL_PYROSCOPE_ADD_PROFILE_BASELINE_URL: false
      OTEL_PYROSCOPE_START_PROFILING: true
      PYROSCOPE_SERVER_ADDRESS: http://pyroscope:4040 # (7)
    # ...
    entrypoint:
      - java
      - -javaagent:/pyroscope.jar # < Add
      - -javaagent:/opentelemetry-javaagent.jar
      - -Dotel.instrumentation.logback-appender.experimental-log-attributes=true
      - -Dotel.instrumentation.logback-appender.experimental.capture-mdc-attributes=*
      - -Dotel.metric.export.interval=5000
      - -cp
      - app:app/lib/*
      - com.worldline.easypay.EasypayServiceApplication
```

1. Define an application name (this will create the `service_name` label),
2. Set format: JFR allows to have multiple events to be recorded,
3. Type of event to profile: `wall` allows to record the time spent in methods. Other valid values are `itimer` and
   `cpu`.
4. Threshold to record lock events,
5. Threshold to record memory events,
6. Declare the Pyroscope extension for the OpenTelemetry agent,
7. Server address.

🛠️ Redeploy `easypay-service`:

```bash
$ docker compose up -d easypay-service
```

✅ Check logs for correct startup:

```bash
docker compose logs -f easypay-service
```

You should see additional logs related to Pyroscope.

👀 Go back to the Pyroscope dashboard (port `4040`):

* In the top menu, you should be able to select the `easypay-service` application,
* Try to display TPU profiling.