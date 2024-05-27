package com.worldline.easypay.payment.control;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.payment.control.bank.BankAuthorService;
import com.worldline.easypay.payment.control.track.PaymentTracker;
import com.worldline.easypay.payment.entity.Payment;
import com.worldline.easypay.payment.entity.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    private CardValidator cardValidator;
    private PosValidator posValidator;
    private PaymentRepository paymentRepository;
    private BankAuthorService bankAuthorService;
    private PaymentTracker paymentTracker;

    @Value("${payment.author.threshold:10000}")
    Integer authorThreshold;

    public PaymentService(CardValidator cardValidator, PosValidator posValidator,
            PaymentRepository paymentRepository, BankAuthorService bankAuthorService, PaymentTracker paymentTracker) {
        this.cardValidator = cardValidator;
        this.posValidator = posValidator;
        this.paymentRepository = paymentRepository;
        this.bankAuthorService = bankAuthorService;
        this.paymentTracker = paymentTracker;
    }

    private void process(PaymentProcessingContext context) {

        if (!posValidator.isActive(context.posId)) {
            context.responseCode = PaymentResponseCode.INACTIVE_POS;
            return;
        }

        if (!cardValidator.checkCardNumber(context.cardNumber)) {
            context.responseCode = PaymentResponseCode.INVALID_CARD_NUMBER;
            return;
        }

        CardType cardType = cardValidator.checkCardType(context.cardNumber);
        if (cardType == CardType.UNKNOWN) {
            context.responseCode = PaymentResponseCode.UNKNWON_CARD_TYPE;
            return;
        }
        context.cardType = cardType;

        if (cardValidator.isBlackListed(context.cardNumber)) {
            context.responseCode = PaymentResponseCode.BLACK_LISTED_CARD_NUMBER;
            return;
        }

        if (context.amount > authorThreshold) {
            if (!bankAuthorService.authorize(context)) {
                // Authorization refused: locally (AMOUNT_EXCEEDED) or remotely by the Bank
                // (AUTHORIZATION_DENIED)?
                // log.info("JJS => refused authorization, context=" + context);
                context.responseCode = context.processingMode.equals(ProcessingMode.STANDARD)
                        ? PaymentResponseCode.AUTHORIZATION_DENIED
                        : PaymentResponseCode.AMOUNT_EXCEEDED;
            }
        }

    }

    private void store(PaymentProcessingContext context) {
        Payment payment = new Payment();

        payment.amount = context.amount;
        payment.cardNumber = context.cardNumber;
        payment.expiryDate = context.expiryDate;
        payment.responseCode = context.responseCode;
        payment.processingMode = context.processingMode;
        payment.cardType = context.cardType;
        payment.posId = context.posId;
        payment.dateTime = context.dateTime;
        payment.responseTime = System.currentTimeMillis() - context.responseTime;
        context.responseTime = payment.responseTime;
        payment.bankCalled = context.bankCalled;
        payment.authorized = context.authorized;
        if (context.authorId.isPresent()) {
            payment.authorId = context.authorId.get();
        }

        paymentRepository.saveAndFlush(payment);

        context.id = payment.id;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    // @TrackedPayment
    public void accept(PaymentProcessingContext paymentContext) {
        process(paymentContext);
        store(paymentContext);
        paymentTracker.track(paymentContext);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public long count() {
        return paymentRepository.count();
    }

}
