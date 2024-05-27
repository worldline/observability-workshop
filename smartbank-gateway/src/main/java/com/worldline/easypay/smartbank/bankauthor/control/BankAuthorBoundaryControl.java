package com.worldline.easypay.smartbank.bankauthor.control;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Long count() {
        return this.repository.count();
    }

    public List<BankAuthorResponse> findAll() {
        return this.repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
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
        return new BankAuthorResponse(author.id, author.merchantId, author.cardNumber, author.expiryDate, author.amount,
                author.authorized);
    }
}
