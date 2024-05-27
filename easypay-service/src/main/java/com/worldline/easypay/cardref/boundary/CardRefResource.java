package com.worldline.easypay.cardref.boundary;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worldline.easypay.cardref.control.CardService;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/cards")
public class CardRefResource {

    CardService cardService;

    public CardRefResource(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(description = "List all cards declared as reference data in the system", summary = "List all cards")
    public ResponseEntity<List<CardRefResponse>> findAll() {
        return ResponseEntity.ok(cardService.listAll());
    }

}
