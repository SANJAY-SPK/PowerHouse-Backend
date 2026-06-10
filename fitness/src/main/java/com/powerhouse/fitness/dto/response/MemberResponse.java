package com.powerhouse.fitness.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate joinDate;
    private String assignedTrainer;
    private String notes;
    private String status;
    private String planName;
    private String planType;
    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalDate lastCheckIn;
    private int totalVisitsThisMonth;
    private int daysRemaining;
}