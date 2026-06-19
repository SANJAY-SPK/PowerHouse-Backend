package com.powerhouse.fitness.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartPoint {
    private String label;   // e.g. "Jan", "2026-06-01", "Week 24"
    private double value;
}
