package com.worldline.easypay.merchantbo.payment.boundary;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentSubscriber {

    @Bean
    public Consumer<String> paymentConsumer() {
        return s -> System.out.println(s);
    }
    
}
