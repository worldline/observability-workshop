package com.worldline.easypay.smartbank.bankauthor.control;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.worldline.easypay.smartbank.bankauthor.boundary.BankAuthorRequest;
import com.worldline.easypay.smartbank.bankauthor.entity.BankAuthor;
import com.worldline.easypay.smartbank.bankauthor.entity.BankAuthorRepository;

import jakarta.transaction.Transactional;

@Service
public class AuthorizationService {

    private BankAuthorRepository repository;

    private Integer maxAmount;
    
    public AuthorizationService(BankAuthorRepository repository, @Value("${smartbank.bankauthor.validation.maxAmount:40000}") Integer maxAmount) {
        this.repository = repository;
        this.maxAmount = maxAmount;
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public boolean authorize(BankAuthorRequest bankAuthorRequest) {
        var authorized = bankAuthorRequest.amount() <= maxAmount;

        // Record the bank author
        BankAuthor author = new BankAuthor();
        author.dateTime = LocalDateTime.now();
        author.merchantId = bankAuthorRequest.merchantId();
        author.amount = bankAuthorRequest.amount();
        author.cardNumber = bankAuthorRequest.cardNumber();
        author.authorized = authorized;
        author.expiryDate = bankAuthorRequest.expiryDate();

        this.repository.save(author);

        return authorized;
    }
}
