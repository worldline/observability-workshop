package com.worldline.easypay.cardref.entity;

import com.worldline.easypay.cardref.control.CardType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_ref")
public class CardRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name="card_number", unique = true)
    public String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name="card_type")
    public CardType cardType;

    @Column(name="blacklisted")
    public Boolean blackListed;
    
}
