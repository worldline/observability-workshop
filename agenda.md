# Observability with Grafana

## Targets

- Stack: Java/Spring Boot
  - Microservices
  - Real database
- Observability Backend: Grafana stack (Loki/Tempo/Prometheus|Mimir/  Pyroscope?)

## Ideas

- Base application can be Spring Petclinic in microservices flavor
    - We  may introduce issues (null pointer, n+1, etc.)
        - Spring Petclinic had the n+1 issue
        - Try to inject data which may produces null exception

- If we don’t want to, what kind of app?
  - Payment ? cf. JF easypay stack
    - Interesting as it involves other services + kafka (for fraudetect)
    - **Should we add some UI?**

- What issues to present?
  - Technical (NullPointer…)
  - N+1
  - Slow queries?
  - Slow tiers?
  - GC?

## Two ways for observability with OTEL

- Java agent: do not require to modify the application but the deployment (java option)
  - Opentelemetry agent
    - URL filtering???
  - Elastic APM agent

- Use Spring Boot modules
  - À voir si on doit compléter

## Architecture

- Application
  - Java Agent?
- Collector (OTEL or -Grafana Agent- or Grafana Alloy)
  - Schema
  - Tail-sampling 
  - Protect Backend
  - Handle auth
  - ETL
- Grafana Stack
  - Mimir Or Prometheus
  - Loki
  - Tempo
  - ?Pyroscope
- Injector
  - k6 ?

## Logs

- What are wrong logs?
  - Non-meaningful
  - No context
  - Toto/Tata/Prout
- Struct log approach
  - How to do that in Java?
    - MDC?
    - Other frameworks?
  - Export to JSON -> better for ingestion
- Show how it is awful to have bad logs into a dashboard/concentrator
- + Correlation Id
- In case of issues, it may better to have logs concentrated in a single place
  - Especially in case of incident to avoid
  - GKE/K8S/…
  - Stacks:
    - EL/FK but requires indexation
    - Loki does not require indexation
    - Cost
    - Qwickwit?
    - VictoriaLogs? :D
- OTEL logs support?

- On voit le nullPointer avec son context ! Trop cool :)

## Metrics

- Why
  - Alert
- Different levels of metrics
  - System
  - JVM
  - Applicative
- Export metrics

- Framework
  - Micrometer

- -> Voir nos pb de performances (GC, temps http…)
  - Alertes

## Traces

- Why
  - Complex systems
  - …
- Export traces to Tempo
- Exemplar Metrics / TraceId Logs

- Tempo vs world?

- Ce qu’on voit:
  - Distributed traces
  - Correlation id / JVM -> trace id / Global
  - n+1
  - slow query

- Résolution??? Dépend de la complexité
  - Peut être juste oral/expliqué mais pas pratiqué

## Correlation

- How to

## GraalVM

- ?? 
- /!\ Temps de compilation
- Tree shaking
- On ne le fait pas faire, mais demo de notre côté

## Bonus - Demo

- Observability from frontend :)
- Pyroscope