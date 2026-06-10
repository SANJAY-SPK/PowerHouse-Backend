package com.powerhouse.fitness.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PlanType type;

    private int durationDays;
    private double price;
    private String features;
    private boolean active = true;

    public enum PlanType {
        MONTHLY, QUARTERLY, ANNUAL
    }
}