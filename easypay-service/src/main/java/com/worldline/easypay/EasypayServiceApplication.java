package com.worldline.easypay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.worldline.easypay.payment.control.bank.BankAuthorClient;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class EasypayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EasypayServiceApplication.class, args);
	}

}
