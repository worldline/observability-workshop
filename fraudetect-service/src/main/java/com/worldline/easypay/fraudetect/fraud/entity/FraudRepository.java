package com.worldline.easypay.fraudetect.fraud.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudRepository extends JpaRepository<Fraud, Long> {
    
}
