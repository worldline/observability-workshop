package com.worldline.easypay.cardref.boundary;

public record CardRefResponse(
        String cardNumber,
        String cardType,
        Boolean blackListed) {

}
