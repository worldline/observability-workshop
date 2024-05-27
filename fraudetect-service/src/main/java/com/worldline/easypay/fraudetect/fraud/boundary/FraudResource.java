package com.worldline.easypay.fraudetect.fraud.boundary;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worldline.easypay.fraudetect.fraud.entity.Fraud;
import com.worldline.easypay.fraudetect.fraud.entity.FraudRepository;

@RestController
@RequestMapping("/fraud")
public class FraudResource {

    FraudRepository fraudRepository;

    public FraudResource(FraudRepository fraudRepository) {
        this.fraudRepository = fraudRepository;
    }

    @GetMapping
    // @Operation(description = "List all fraud records processed by the system",
    // summary="List fraud records")
    public ResponseEntity<List<Fraud>> findAll() {
        return ResponseEntity.ok().body(fraudRepository.findAll());
    }

    @GetMapping("/count")
    // @Operation(description = "Count all fraud records processed by the system",
    // summary="Count fraud records")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok().body(fraudRepository.count());
    }

}
