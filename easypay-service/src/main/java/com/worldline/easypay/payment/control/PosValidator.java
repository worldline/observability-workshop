package com.worldline.easypay.payment.control;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;

import com.worldline.easypay.posref.entity.PosRef;
import com.worldline.easypay.posref.entity.PosRefRepository;

@Controller
public class PosValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PosValidator.class);

    private PosRefRepository posRefRepository;

    public PosValidator(PosRefRepository posRefRepository) {
        this.posRefRepository = posRefRepository;
    }
    
    public boolean isActive(String posId) { 
        PosRef probe = new PosRef();
        probe.posId = posId;
        List<PosRef> posList = posRefRepository.findAll(Example.of(probe));
     
        if (posList.isEmpty()) {
//            LOG.warn( "Check POS does not pass: unknown posId {}", posId);
            return false;
        }

        boolean result = posList.get(0).active;

//        if (!result) {
//            LOG.warn( "Check POS does not pass: inactive posId {}", posId);
//        }

        return result;

    }
}
