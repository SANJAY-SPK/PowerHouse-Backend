package com.powerhouse.fitness.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private double amount;
    private LocalDate date;
    private String planName;
    private String status;
    private String mode;
}