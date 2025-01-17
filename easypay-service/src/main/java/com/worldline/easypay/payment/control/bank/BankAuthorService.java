package com.worldline.easypay.payment.control.bank;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.worldline.easypay.payment.control.PaymentProcessingContext;
import com.worldline.easypay.payment.control.ProcessingMode;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class BankAuthorService {

    private static final Logger LOG = LoggerFactory.getLogger(BankAuthorService.class);

    @Value("${payment.author.merchantId:Snowcamp Swag Store}")
    String merchantId;

    @Value("${payment.max.amount.fallback:20000}")
    Long maxAmountFallback;

    private BankAuthorClient client;

    public BankAuthorService(BankAuthorClient client) {
        this.client = client;
    }

    private BankAuthorRequest initRequest(PaymentProcessingContext context) {
        return new BankAuthorRequest(
                merchantId,
                context.cardNumber,
                context.expiryDate,
                context.amount);
    }

    @Retry(name = "BankAuthorService", fallbackMethod = "acceptByDelegation")
    public boolean authorize(PaymentProcessingContext context) {
        LOG.info("Request payment authorization to bank, with context: {}", context);
        try {
            var response = client.authorize(initRequest(context));
            context.bankCalled = true;
            context.authorId = Optional.of(response.authorId());
            context.authorized = response.authorized();
            LOG.info("Bank answered with authorId: {}, authorized: {}", response.authorId(), response.authorized());
            return context.authorized;
        } catch (Exception e) {
            LOG.warn("Exception while requesting bank, operation will be retried or fallback: {}", e.getMessage());
            throw e;
        }
    }

    public boolean acceptByDelegation(PaymentProcessingContext context, Throwable throwable) {
        LOG.warn("Accept by delegation. Error was: {}", throwable.getMessage());        
        context.bankCalled = false;
        context.processingMode = ProcessingMode.FALLBACK;
        return context.amount < maxAmountFallback;
    }
}
