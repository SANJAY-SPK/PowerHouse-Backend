package com.powerhouse.fitness.controller;

import com.powerhouse.fitness.dto.request.PaymentRequest;
import com.powerhouse.fitness.dto.response.PaymentResponse;
import com.powerhouse.fitness.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PaymentResponse>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(paymentService.getPaymentsByMember(memberId));
    }

    @GetMapping("/due")
    public ResponseEntity<List<PaymentResponse>> getDue() {
        return ResponseEntity.ok(paymentService.getDuePayments());
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> record(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.recordPayment(request));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentResponse> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.markAsPaid(id));
    }
}