package com.worldline.easypay.cardref.control;

import java.util.List;

import org.springframework.stereotype.Service;

import com.worldline.easypay.cardref.boundary.CardRefResponse;
import com.worldline.easypay.cardref.entity.CardRef;
import com.worldline.easypay.cardref.entity.CardRefRepository;

@Service
public class CardService {

    CardRefRepository repository;
    
    public CardService(CardRefRepository repository) {
        this.repository = repository;
    }

    public List<CardRefResponse> listAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private CardRefResponse toResponse(CardRef cardRef) {
        return new CardRefResponse(cardRef.cardNumber, cardRef.cardType.toString(), cardRef.blackListed);
    }
}
