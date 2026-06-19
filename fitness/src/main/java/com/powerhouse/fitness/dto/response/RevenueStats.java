package com.powerhouse.fitness.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RevenueStats {
    private String  period;            // "OVERALL" | "YEARLY" | "MONTHLY" | "WEEKLY" | "DAILY"
    private String  label;             // Human-readable label e.g. "June 2026"
    private double  totalCollected;
    private double  totalPending;
    private long    transactionCount;
    private List<ChartPoint> chartData; // bars for the selected period
}
