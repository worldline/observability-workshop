package com.worldline.easypay.smartbank.bankauthor.boundary;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.worldline.easypay.smartbank.bankauthor.control.AuthorizationService;
import com.worldline.easypay.smartbank.bankauthor.control.BankAuthorBoundaryControl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


@RestController
@RequestMapping("/authors")
public class BankAuthorResource {

    BankAuthorBoundaryControl bankAuthors;
    AuthorizationService authorizationService;

    public BankAuthorResource(BankAuthorBoundaryControl boundaryControl, AuthorizationService validationService) {
        this.bankAuthors = boundaryControl;
        this.authorizationService = validationService;
    }

    @GetMapping("/count")
    @Operation(summary = "")
    public ResponseEntity<BankAuthorCountResponse> count() {
        return ResponseEntity.ok().body(new BankAuthorCountResponse(this.bankAuthors.count()));
    }


    @GetMapping
    @Operation(summary = "Get all payment authorizations", description = "Get all payment authorizations")
    @ApiResponse(responseCode = "200", description = "List of payment authorizations found")
    public ResponseEntity<List<BankAuthorResponse>> findAll() {
        return ResponseEntity.ok().body(this.bankAuthors.findAll());
    }


    @GetMapping(value = "/{id}")
    @Operation(summary = "Get a payment authorization", description = "Get a payment authorization by its ID")
    @ApiResponse(responseCode = "200", description = "Payment authorization found")
    public ResponseEntity<BankAuthorResponse> findById(
            @Parameter(description = "The ID of the payment authorization to retrieve") @PathVariable("id") String id) {
        try {
            var authorizationId = UUID.fromString(id);
            Optional<BankAuthorResponse> response = this.bankAuthors.findById(authorizationId);
            if (response.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().body(response.get());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/authorize")
    @Operation(summary = "Process a payment authorization", description = "Deliver (or refuse) a payment authorization request")
    @ApiResponse(responseCode = "201", description = "Payment authorization request processed successfully")
    @Transactional
    ResponseEntity<BankAuthorResponse> authorize(@Valid @NotNull @RequestBody BankAuthorRequest request) {
        var authorized = this.authorizationService.authorize(request);
        var response = new BankAuthorResponse(UUID.randomUUID(), request.merchantId(), request.cardNumber(),
                request.expiryDate(), request.amount(), authorized);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.authorId()).toUri();
        return ResponseEntity.created(location).body(response);
    }
}
