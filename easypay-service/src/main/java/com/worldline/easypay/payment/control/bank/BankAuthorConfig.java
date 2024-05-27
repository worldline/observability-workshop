package com.worldline.easypay.payment.control.bank;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

// @Configuration
public class BankAuthorConfig {

    //@Bean
    //@LoadBalanced
    public RestClient.Builder restClient() {
        return RestClient.builder();
    }


    // @Bean
    BankAuthorClient produceBankAuthorClient(RestClient.Builder restClient) {
        RestClientAdapter adapter = RestClientAdapter.create(restClient.build());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(BankAuthorClient.class);
    }
    
}
