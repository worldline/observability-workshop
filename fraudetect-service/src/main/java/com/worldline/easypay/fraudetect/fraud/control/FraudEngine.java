package com.worldline.easypay.fraudetect.fraud.control;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.worldline.easypay.fraudetect.fraud.boundary.PaymentEvent;
import com.worldline.easypay.fraudetect.fraud.entity.Fraud;
import com.worldline.easypay.fraudetect.fraud.entity.FraudRepository;

import jakarta.transaction.Transactional;

@Service
public class FraudEngine {

    private static final Logger log = LoggerFactory.getLogger(FraudEngine.class);

    FraudRepository fraudRepository;
    Long countThreshold;

    public FraudEngine(FraudRepository fraudRepository, @Value("${fraud.count.threshold:2}") Long countThreshold) {
        this.fraudRepository = fraudRepository;
        this.countThreshold = countThreshold;
    }

    @Transactional
    public void detect(PaymentEvent event) {
        var probe = new Fraud();
        probe.cardNumber = event.cardNumber();

        Optional<Fraud> dbRecord = fraudRepository.findOne(Example.of(probe));
        Fraud record;

        if (dbRecord.isPresent()) {
            record = dbRecord.get();
            record.count += 1L;
        } else {
            record = new Fraud();
            record.cardNumber = event.cardNumber();
            record.count = 1L;
            record.status = FraudStatus.GREEN;
            record = fraudRepository.saveAndFlush(record);
        }

        if (record.status == FraudStatus.GREEN) {
            if (record.count.longValue() >= countThreshold) {
                record.status = FraudStatus.ORANGE;
            }
        } else if (record.status == FraudStatus.ORANGE) {
            if (record.count.longValue() >= (2 * countThreshold)) {
                record.status = FraudStatus.RED;
            }
        }

        fraudRepository.save(record);

        log.warn("FraudEngine => {}", record);
    }

}
