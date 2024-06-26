package com.worldline.easypay.discoveryserver.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.prometheus.metrics.tracer.otel_agent.OpenTelemetryAgentSpanContext;

/**
 * Registers OpenTelemetry Agent Span context
 * 
 * Adds exemplar support for metrics by providing the
 * OpenTelemetryAgentSpanContext bean
 * when opentelemetry agent is available.
 */
@Configuration
public class PrometheusRegistryConfiguration {

    @Bean
    @ConditionalOnClass(name = "io.opentelemetry.javaagent.shaded.io.opentelemetry.api.trace.Span")
    public OpenTelemetryAgentSpanContext exemplarConfigSupplier() {
        return new OpenTelemetryAgentSpanContext();
    }
}
