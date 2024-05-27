package com.worldline.easypay.fraudetect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class FraudetectServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudetectServiceApplication.class, args);
	}

}
