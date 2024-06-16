package com.worldline.easypay.merchantbo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PrintConfig {

    private static final Logger LOG 
      = LoggerFactory.getLogger(PrintConfig.class);
    
    private Environment environment;

    public PrintConfig(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        LOG.info("Property spring.cloud.stream.kafka.binder.brokers is {}", environment.getProperty("spring.cloud.stream.kafka.binder.brokers"));
    }
}
