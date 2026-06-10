package com.powerhouse.fitness.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull private Long memberId;
    @NotNull private double amount;
    private String planName;
    private String mode; // CASH, UPI, CARD
}