package com.worldline.easypay.payment.control.bank;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("SMARTBANK-GATEWAY")
public interface BankAuthorClient {

    @PostMapping("/authors/authorize")
    BankAuthorResponse authorize(@RequestBody BankAuthorRequest request);

    @GetMapping("/authors/count")
    long count();
}
