package com.worldline.easypay.merchantbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MerchantBackofficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MerchantBackofficeApplication.class, args);
	}

}
