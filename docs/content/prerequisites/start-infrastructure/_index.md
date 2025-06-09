+++
date = '2025-06-06T22:23:20+02:00'
title = 'Start Infrastructure'
weight = 3
+++

The "infrastructure stack" is composed of the following components:

* One [PostgreSQL](https://www.postgresql.org/) instance per micro service
* One [Kafka broker](https://kafka.apache.org/)
* One [Service Discovery](https://spring.io/guides/gs/service-registration-and-discovery) microservice to enable load
  balancing & loose coupling.
* One [Configuration server](https://docs.spring.io/spring-cloud-config/) is also used to centralise the configuration
  of our microservices.
* The following microservices: API Gateway, Merchant BO, Fraud Detect, Smart Bank Gateway

🛠️ Execute the following commands

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
NAME                                               IMAGE                                         COMMAND                  SERVICE                   CREATED         STATUS                        PORTS
api-gateway                                        api-gateway:latest                            "java -javaagent:/op…"   api-gateway               3 minutes ago   Up 2 minutes (healthy)        0.0.0.0:8080->8080/tcp, :::8080->8080/tcp
config-server                                      config-server:latest                          "java -javaagent:/op…"   config-server             3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:8888->8888/tcp, :::8888->8888/tcp
discovery-server                                   discovery-server:latest                       "java -javaagent:/op…"   discovery-server          3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:8761->8761/tcp, :::8761->8761/tcp
easypay-service                                    easypay-service:latest                        "java -cp app:app/li…"   easypay-service           3 minutes ago   Up 2 minutes (healthy)        
fraudetect                                         fraudetect-service:latest                     "java -javaagent:/op…"   fraudetect-service        3 minutes ago   Up 2 minutes (healthy)        
kafka                                              confluentinc/cp-kafka:7.6.1                   "/etc/confluent/dock…"   kafka                     3 minutes ago   Up 3 minutes (healthy)        9092/tcp, 0.0.0.0:19092->19092/tcp, :::19092->19092/tcp
merchant-backoffice                                merchant-backoffice:latest                    "java -javaagent:/op…"   merchant-backoffice       3 minutes ago   Up 2 minutes (healthy)        
observability-workshop-grafana-1                   grafana/grafana:latest                        "sh -xeuc 'mkdir -p …"   grafana                   3 minutes ago   Up 3 minutes                  0.0.0.0:3000->3000/tcp, :::3000->3000/tcp
observability-workshop-loki-1                      grafana/loki:latest                           "/usr/bin/loki -conf…"   loki                      3 minutes ago   Up 3 minutes                  0.0.0.0:3100->3100/tcp, :::3100->3100/tcp
observability-workshop-opentelemetry-collector-1   otel/opentelemetry-collector-contrib:latest   "/otelcol-contrib --…"   opentelemetry-collector   3 minutes ago   Up 3 minutes                  0.0.0.0:4317-4318->4317-4318/tcp, :::4317-4318->4317-4318/tcp, 55678-55679/tcp
observability-workshop-postgres-easypay-1          postgres:16                                   "docker-entrypoint.s…"   postgres-easypay          3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:5432->5432/tcp, :::5432->5432/tcp
observability-workshop-postgres-fraudetect-1       postgres:16                                   "docker-entrypoint.s…"   postgres-fraudetect       3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:5434->5432/tcp, [::]:5434->5432/tcp
observability-workshop-postgres-merchantbo-1       postgres:16                                   "docker-entrypoint.s…"   postgres-merchantbo       3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:5435->5432/tcp, [::]:5435->5432/tcp
observability-workshop-postgres-smartbank-1        postgres:16                                   "docker-entrypoint.s…"   postgres-smartbank        3 minutes ago   Up 3 minutes (healthy)        0.0.0.0:5433->5432/tcp, [::]:5433->5432/tcp
observability-workshop-prometheus-1                prom/prometheus:latest                        "/bin/prometheus --c…"   prometheus                3 minutes ago   Up 3 minutes                  0.0.0.0:9090->9090/tcp, :::9090->9090/tcp
observability-workshop-tempo-1                     grafana/tempo:latest                          "/tempo -config.file…"   tempo                     3 minutes ago   Up 3 minutes                  0.0.0.0:3200->3200/tcp, :::3200->3200/tcp, 0.0.0.0:9095->9095/tcp, :::9095->9095/tcp, 0.0.0.0:9411->9411/tcp, :::9411->9411/tcp
smartbank-gateway                                  smartbank-gateway:latest                      "java -Xmx4g -cp app…"   smartbank-gateway         3 minutes ago   Up 2 minutes (healthy)
```

## Troubleshooting

### Gradle build error

You may encounter Gradle build errors due to a timeout when trying to get the lock on some files (as all the services are built concurrently). You can either:

- Execute the `docker compose up -d --build` command each time you have the error until the environment is started,
- Or disable concurrency by executing the two following commands:
  - `./scripts/build-services.sh` (takes a long time as all services are built one by one),
  - `docker compose up -d --build` (should work this time as all services are built by the previous command).