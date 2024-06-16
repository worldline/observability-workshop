package com.worldline.easypay.payment.control.bank;

public record BankAuthorRequest(
    String merchantId,
    String cardNumber,
    String expiryDate,
    Integer amount
) {
}
