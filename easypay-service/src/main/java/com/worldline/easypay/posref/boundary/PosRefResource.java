package com.worldline.easypay.posref.boundary;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.worldline.easypay.posref.control.PosService;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/pos")
public class PosRefResource {

    private static final Logger LOG = LoggerFactory.getLogger(PosRefResource.class);

    PosService posService;

    public PosRefResource(PosService posService) {
        this.posService = posService;
    }

    @GetMapping
    @Operation(description = "List all Point of Sales declared in the system", summary = "List Point of Sales")
    public ResponseEntity<List<PosRefResponse>> findAll() {
        LOG.info("Request: list all POS");
        return ResponseEntity.ok(posService.findAll());
    }

}
