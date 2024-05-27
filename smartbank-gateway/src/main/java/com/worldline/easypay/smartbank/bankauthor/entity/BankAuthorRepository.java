package com.worldline.easypay.smartbank.bankauthor.entity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAuthorRepository extends JpaRepository<BankAuthor, UUID> {
    
}
