+++
title = "Profiling (Bonus)"
type = "chapter"
weight = 7
+++

The OpenTelemetry project standardizes telemetry signals, particularly the logs, metrics, and traces we have seen so
far.  
However, last year [they announced their work on a fourth signal: profiling](https://opentelemetry.io/blog/2024/profiling/).

Profiling involves measuring the performance characteristics of your application, such as execution time, CPU
utilization, or memory usage. It helps identify bottlenecks and optimize resource usage in your application.

If you're familiar with Java, you may already know [async_profiler](https://github.com/async-profiler/async-profiler)
for HotSpot JVMs. It defines itself as a "low overhead sampling profiler for Java."

You may have also heard about eBPF, a technology embedded in the Linux kernel that allows running code in a sandbox
within the kernel space. This technology is gaining traction in service meshes and in continuous profiling.

Continuous profiling is an ongoing area of interest in the observability field, aimed at finding additional performance
improvements.

### (Grafana) Pyroscope

Pyroscope was an open-source project for continuous profiling. It consists of a server that receives profiling samples,
which can then be analyzed and displayed as a [flamegraph](https://www.brendangregg.com/flamegraphs.html).

In the Java landscape, it offers a Java Agent based on *async_profiler*, compatible with other agents such as the
OpenTelemetry agent. Phew!

In 2023, Grafana acquired Pyroscope and merged it with its own solution, Phlare. Welcome
to [Grafana Pyroscope](https://grafana.com/docs/pyroscope/latest/)!

If you want to know more about Continuous Profiling and what it can bring to you, you may want to check out
the [Grafana Pyroscope documentation](https://grafana.com/docs/pyroscope/latest/introduction/profiling/).

### Objectives

In this section, we aim to show you:

* What profiling is,
* What a flamegraph is,
* How it integrates in Grafana.

{{% children containerstyle="div" style="h3" description=false %}}