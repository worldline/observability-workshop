package com.worldline.easypay.payment.boundary;

import java.util.Optional;
import java.util.UUID;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.payment.control.PaymentResponseCode;
import com.worldline.easypay.payment.control.ProcessingMode;

public record PaymentResponse(
        String posId,
        String cardNumber,
        String expiryDate,
        Integer amount,

        UUID paymentId,
        PaymentResponseCode responseCode,
        Optional<UUID> authorId,
        CardType cardType,
        Boolean bankCalled,
        Boolean authorized,
        Long responseTime,
        ProcessingMode processingMode) {

}
