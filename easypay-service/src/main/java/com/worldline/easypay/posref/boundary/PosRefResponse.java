package com.worldline.easypay.posref.boundary;

public record PosRefResponse(
        String posId,
        String location,
        Boolean active) {

}
