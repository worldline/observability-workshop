package com.worldline.easypay.payment.control.bank;

import java.util.UUID;

public record BankAuthorResponse(
    String cardNumber,
    String expiryDate,
    Integer amount,
    UUID authorId,
    Boolean authorized
) {
}
