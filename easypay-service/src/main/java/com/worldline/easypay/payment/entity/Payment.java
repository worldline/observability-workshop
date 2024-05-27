package com.worldline.easypay.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.worldline.easypay.cardref.control.CardType;
import com.worldline.easypay.payment.control.PaymentResponseCode;
import com.worldline.easypay.payment.control.ProcessingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "date_time")
    public LocalDateTime dateTime;

    @Column(name = "response_time")
    public Long responseTime;

    @Column(name = "pos_id")
    public String posId;

    @Column(name = "card_number")
    public String cardNumber;

    @Column(name = "expiry_date")
    public String expiryDate;

    @Column(name = "amount")
    public Integer amount;

    @Column(name = "card_type")
    @Enumerated(EnumType.STRING)
    public CardType cardType;

    @Column(name = "response_code")
    @Enumerated(EnumType.STRING)
    public PaymentResponseCode responseCode;

    @Column(name = "processing_mode")
    @Enumerated(EnumType.STRING)
    public ProcessingMode processingMode;

    @Column(name = "bank_called")
    public Boolean bankCalled;

    @Column(name = "authorized")
    public Boolean authorized;

    @Column(name = "authorization_id")
    public UUID authorId;
}
