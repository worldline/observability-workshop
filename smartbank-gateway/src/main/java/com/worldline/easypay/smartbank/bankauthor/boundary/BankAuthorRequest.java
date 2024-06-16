package com.worldline.easypay.smartbank.bankauthor.boundary;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BankAuthorRequest(
        @NotNull String merchantId,
        @NotNull String cardNumber,
        @NotNull String expiryDate,
        @Min(10) Integer amount) {

}
