package com.worldline.easypay.payment.control.track;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.payment.control.PaymentResponseCode;
import com.worldline.easypay.payment.control.ProcessingMode;

public record PaymentProcessedEvent(
        // Internal data coming from PaymentProcessingContext
        String posId,
        String cardNumber,
        String expiryDate,
        int amount,
        UUID paymentId,
        PaymentResponseCode responseCode,
        CardType cardType,
        ProcessingMode processingMode,
        long responseTime,
        LocalDateTime dateTime,
        boolean bankCalled,
        boolean authorized,
        Optional<UUID> authorId,

        // Additional configuration data
        String paymentServerId) {
}
