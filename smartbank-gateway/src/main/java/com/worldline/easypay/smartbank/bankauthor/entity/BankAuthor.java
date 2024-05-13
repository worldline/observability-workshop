package com.worldline.easypay.smartbank.bankauthor.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bank_author")
public class BankAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "date_time")
    public LocalDateTime dateTime;

    @Column(name = "merchant_id")
    public String merchantId;

    @Column(name = "card_number")
    public String cardNumber;

    @Column(name = "expiry_date")
    public String expiryDate;
    
    public Integer amount;
    public Boolean authorized;
}
