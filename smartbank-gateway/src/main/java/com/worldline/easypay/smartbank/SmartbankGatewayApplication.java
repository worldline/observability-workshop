package com.worldline.easypay.smartbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SmartbankGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartbankGatewayApplication.class, args);
	}

}
