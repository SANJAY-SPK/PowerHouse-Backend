package com.powerhouse.fitness.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {
    private long totalMembers;
    private long activeMembers;
    private long expiringThisWeek;
    private long expiredMembers;
    private long overduePayments;
    private double totalCollected;
    private double totalPending;
}