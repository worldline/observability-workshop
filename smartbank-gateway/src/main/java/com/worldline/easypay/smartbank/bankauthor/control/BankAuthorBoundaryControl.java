package com.worldline.easypay.smartbank.bankauthor.control;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.worldline.easypay.smartbank.bankauthor.boundary.BankAuthorResponse;
import com.worldline.easypay.smartbank.bankauthor.entity.BankAuthor;
import com.worldline.easypay.smartbank.bankauthor.entity.BankAuthorRepository;

@Service
public class BankAuthorBoundaryControl {
    
    private BankAuthorRepository repository;

    public BankAuthorBoundaryControl(BankAuthorRepository repository) {
        this.repository = repository;
    }

    public Optional<BankAuthorResponse> findById(UUID uuid) {
        Optional<BankAuthor> author = this.repository.findById(uuid);

        if (author.isPresent()) {
            return Optional.of(toResponse(author.get()));
        } else {
            return Optional.empty();
        }
    }

    private BankAuthorResponse toResponse(BankAuthor author) {
        return new BankAuthorResponse(author.id, author.merchantId,author.cardNumber,author.expiryDate, author.amount, author.authorized);
    }
}
