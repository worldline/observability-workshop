package com.worldline.easypay.payment.boundary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull String posId,
        @NotNull String cardNumber,
        @NotNull String expiryDate,
        @Min(10) Integer amount) {
}
