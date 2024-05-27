package com.worldline.easypay.payment.control;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.payment.boundary.PaymentRequest;
import com.worldline.easypay.payment.boundary.PaymentResponse;

public class PaymentProcessingContext {
    // Data coming from the request
    public final String posId;
    public final String cardNumber;
    public final String expiryDate;
    public final int amount;

    // Data generated by the processing
    public UUID id;
    public PaymentResponseCode responseCode;
    public CardType cardType;
    public ProcessingMode processingMode;
    public long responseTime;
    public LocalDateTime dateTime;
    public boolean bankCalled;
    public boolean authorized;
    public Optional<UUID> authorId;

    // Initializes the processing context from the request
    public PaymentProcessingContext(PaymentRequest request) {
        this.posId = request.posId();
        this.dateTime = LocalDateTime.now();
        this.responseTime = System.currentTimeMillis();
        this.cardNumber = request.cardNumber();
        this.expiryDate = request.expiryDate();
        this.amount = request.amount();

        // Default values
        this.responseCode = PaymentResponseCode.ACCEPTED;
        this.processingMode = ProcessingMode.STANDARD;
        this.authorized = false;
        this.bankCalled = false;
        this.authorId = Optional.empty();
    }

    // Generates the response from the processing context
    public PaymentResponse generateResponse() {
        return new PaymentResponse(
                posId,
                cardNumber,
                expiryDate,
                amount,
                id,
                responseCode,
                authorId,
                cardType,
                bankCalled,
                authorized,
                responseTime,
                processingMode);
    }

}
