package com.worldline.easypay.fraudetect.fraud.boundary;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worldline.easypay.fraudetect.fraud.entity.Fraud;
import com.worldline.easypay.fraudetect.fraud.entity.FraudRepository;

@RestController
@RequestMapping("/fraud")
public class FraudResource {

    private static final Logger LOG = LoggerFactory.getLogger(FraudResource.class);

    FraudRepository fraudRepository;

    public FraudResource(FraudRepository fraudRepository) {
        this.fraudRepository = fraudRepository;
    }

    @GetMapping
    public ResponseEntity<List<Fraud>> findAll() {
        LOG.info("Request: get all fraud records");
        return ResponseEntity.ok().body(fraudRepository.findAll());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        LOG.info("Request: get number of fraud records processed by the system");
        return ResponseEntity.ok().body(fraudRepository.count());
    }

}
