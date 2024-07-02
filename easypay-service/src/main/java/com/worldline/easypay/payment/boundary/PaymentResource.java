package com.worldline.easypay.payment.boundary;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.worldline.easypay.payment.control.PaymentProcessingContext;
import com.worldline.easypay.payment.control.PaymentService;
import com.worldline.easypay.payment.entity.Payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/payments")
public class PaymentResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentResource.class);

    PaymentService paymentService;

    public PaymentResource(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(description = "List all payments that have been processed", summary = "List all payments")
    public ResponseEntity<List<Payment>> findAll() {
//        LOG.info("Request: get all processed payments");
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("count")
    @Operation(description = "Count all payments", summary = "Count payments")
    public ResponseEntity<Long> count() {
//        LOG.info("Request: get number of processed payments");
//        return ResponseEntity.ok(paymentService.count());
    }

    @GetMapping("{id}")
    @Operation(description = "Retrieve a given payment with its id", summary = "Retrieve a payment with its id")
    @ApiResponse(responseCode = "200", description = "Payment found", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "204", description = "Payment not found", content = @Content(mediaType = "text/plain"))
    public ResponseEntity<Payment> findById(
            @Parameter(description = "The payment id to be retrieved", required = true) @PathVariable("id") String paymentId) {
//        LOG.info("Request: get payment by id: {}", paymentId);
        UUID id = UUID.fromString(paymentId);
        var payment = paymentService.findById(id);
        if (payment.isEmpty()) {
//            LOG.warn("Payment with id {} not found.", paymentId);
            return ResponseEntity.notFound().build();
        }
//        LOG.debug("Response: found payment: {}", payment.get());
        return ResponseEntity.ok(payment.get());
    }

    @PostMapping
    @Operation(description = "Process a payment: can be accepted or denied", summary = "Process a payment")
    @ApiResponse(responseCode = "201", description = "Payment processed", content = @Content(mediaType = "application/json"))
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "The payment to be processed", required = true) @Valid @NotNull @RequestBody PaymentRequest paymentRequest) {
//        MDC.put("CardNumber",paymentRequest.cardNumber());
//        MDC.put("POS",paymentRequest.posId());
        // LOG.info("Processing new payment: {}", paymentRequest);
        PaymentProcessingContext paymentContext = new PaymentProcessingContext(paymentRequest);

        paymentService.accept(paymentContext);

        PaymentResponse response = paymentContext.generateResponse();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.paymentId()).toUri();
        var httpResponse = ResponseEntity.created(location).body(response);
        // MDC.clear();
        return httpResponse;
    }
}
