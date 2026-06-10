package com.powerhouse.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    private String email;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate joinDate;
    private String assignedTrainer;
    private String notes;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalDate lastCheckIn;
    private int totalVisitsThisMonth;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum MemberStatus {
        ACTIVE, EXPIRING, EXPIRED, PAUSED
    }
}