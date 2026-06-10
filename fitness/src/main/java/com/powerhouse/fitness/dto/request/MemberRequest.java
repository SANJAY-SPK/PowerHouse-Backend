package com.powerhouse.fitness.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MemberRequest {
    @NotBlank private String name;
    @NotBlank private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private String assignedTrainer;
    private String notes;
    @NotNull private Long planId;
    @NotNull private LocalDate planStartDate;
    private double paymentAmount;
    private String paymentMode; // CASH, UPI, CARD
}