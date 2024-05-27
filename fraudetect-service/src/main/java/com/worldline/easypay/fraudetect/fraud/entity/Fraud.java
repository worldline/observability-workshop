package com.worldline.easypay.fraudetect.fraud.entity;

import com.worldline.easypay.fraudetect.fraud.control.FraudStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fraud")
public class Fraud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "card_number", unique = true)
    public String cardNumber;

    @Column(name = "count")
    public Long count;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public FraudStatus status;

    @Override
    public String toString() {
        return "Fraud{" +
                "id=" + id +
                ", cardNumber='" + cardNumber + '\'' +
                ", count=" + count +
                ", status=" + status +
                '}';
    }
}
