package com.worldline.easypay.payment.control.track;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.worldline.easypay.payment.control.PaymentProcessingContext;

@Service
public class PaymentTracker {

    StreamBridge streamBridge;

    String paymentServerId;


    public PaymentTracker(StreamBridge streamBridge, @Value("${payment.server.id:unknown}") String paymentServerId) {
        this.streamBridge = streamBridge;
        this.paymentServerId = paymentServerId;
    }

    public void track(PaymentProcessingContext context) {
        var event = new PaymentProcessedEvent(
                context.posId, 
                context.cardNumber, 
                context.expiryDate, 
                context.amount, 
                context.id, 
                context.responseCode,
                context.cardType, 
                context.processingMode, 
                context.responseTime, 
                context.dateTime, 
                context.bankCalled,
                context.authorized, 
                context.authorId, 
                "unknox"
        );
        streamBridge.send("payment", event);
    }
}
