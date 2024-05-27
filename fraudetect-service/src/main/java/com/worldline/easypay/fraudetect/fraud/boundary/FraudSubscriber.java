package com.worldline.easypay.fraudetect.fraud.boundary;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.worldline.easypay.fraudetect.fraud.control.FraudEngine;

@Configuration
public class FraudSubscriber {

    @Bean
    public Consumer<PaymentEvent> fraudConsumer(FraudEngine fraudEngine) {
        return event -> fraudEngine.detect(event);
    }
}
