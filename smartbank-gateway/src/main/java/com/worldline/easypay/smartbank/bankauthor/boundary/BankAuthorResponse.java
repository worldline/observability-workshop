package com.worldline.easypay.smartbank.bankauthor.boundary;

import java.util.UUID;

public record BankAuthorResponse(
        UUID authorId,
        String merchantId,
        String cardNumber,
        String expiryDate,
        Integer amount,
        Boolean authorized) {

}
