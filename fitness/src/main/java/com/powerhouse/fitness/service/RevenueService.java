package com.powerhouse.fitness.service;

import com.powerhouse.fitness.dto.response.ChartPoint;
import com.powerhouse.fitness.dto.response.RevenueStats;
import com.powerhouse.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueService {

    private final PaymentRepository paymentRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // OVERALL — all-time totals + year-by-year chart
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RevenueStats getOverallRevenue() {
        double collected = nvl(paymentRepository.getTotalCollected());
        double pending   = nvl(paymentRepository.getTotalPending());
        long   count     = nvl(paymentRepository.getTotalPaidCount());

        // Yearly bars
        List<Object[]> rows = paymentRepository.getYearlyBreakdown();
        List<ChartPoint> chart = new ArrayList<>();
        for (Object[] row : rows) {
            int    year  = ((Number) row[0]).intValue();
            double total = ((Number) row[1]).doubleValue();
            chart.add(new ChartPoint(String.valueOf(year), total));
        }

        return RevenueStats.builder()
                .period("OVERALL")
                .label("All Time")
                .totalCollected(collected)
                .totalPending(pending)
                .transactionCount(count)
                .chartData(chart)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // YEARLY — totals for the given year + monthly chart (12 bars)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RevenueStats getYearlyRevenue(int year) {
        double collected = paymentRepository.getYearlyCollected(year);
        double pending   = paymentRepository.getYearlyPending(year);
        long   count     = paymentRepository.getYearlyCount(year);

        // Build a month→value map from DB rows
        Map<Integer, Double> monthMap = new HashMap<>();
        for (Object[] row : paymentRepository.getMonthlyBreakdown(year)) {
            int    month = ((Number) row[0]).intValue();
            double total = ((Number) row[1]).doubleValue();
            monthMap.put(month, total);
        }

        // All 12 months, zero-filled for months with no data
        List<ChartPoint> chart = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            chart.add(new ChartPoint(label, monthMap.getOrDefault(m, 0.0)));
        }

        return RevenueStats.builder()
                .period("YEARLY")
                .label(String.valueOf(year))
                .totalCollected(collected)
                .totalPending(pending)
                .transactionCount(count)
                .chartData(chart)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MONTHLY — totals for the given month + daily chart
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RevenueStats getMonthlyRevenue(int year, int month) {
        double collected = paymentRepository.getMonthlyCollected(year, month);
        double pending   = paymentRepository.getMonthlyPending(year, month);
        long   count     = paymentRepository.getMonthlyCount(year, month);

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        List<ChartPoint> chart = buildDailyChart(from, to, "d");

        String label = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
        return RevenueStats.builder()
                .period("MONTHLY")
                .label(label)
                .totalCollected(collected)
                .totalPending(pending)
                .transactionCount(count)
                .chartData(chart)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WEEKLY — totals for the Mon–Sun week containing `date` + 7-day chart
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RevenueStats getWeeklyRevenue(LocalDate date) {
        LocalDate from = date.with(DayOfWeek.MONDAY);
        LocalDate to   = date.with(DayOfWeek.SUNDAY);

        double collected = paymentRepository.getRangeCollected(from, to);
        double pending   = paymentRepository.getRangePending(from, to);
        long   count     = paymentRepository.getRangeCount(from, to);

        List<ChartPoint> chart = buildDailyChart(from, to, "EEE");

        String label = from + " – " + to;
        return RevenueStats.builder()
                .period("WEEKLY")
                .label(label)
                .totalCollected(collected)
                .totalPending(pending)
                .transactionCount(count)
                .chartData(chart)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DAILY — single-day total (no sub-chart needed; returns one bar)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public RevenueStats getDailyRevenue(LocalDate date) {
        double collected = paymentRepository.getDailyCollected(date);
        double pending   = paymentRepository.getDailyPending(date);
        long   count     = paymentRepository.getDailyCount(date);

        // Single bar for the day
        List<ChartPoint> chart = List.of(new ChartPoint(date.toString(), collected));

        String label = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + ", " + date.getDayOfMonth()
                + " " + date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                + " " + date.getYear();

        return RevenueStats.builder()
                .period("DAILY")
                .label(label)
                .totalCollected(collected)
                .totalPending(pending)
                .transactionCount(count)
                .chartData(chart)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Build a daily chart series between from (inclusive) and to (inclusive).
     * @param labelFormat "d" for day-of-month, "EEE" for Mon/Tue etc.
     */
    private List<ChartPoint> buildDailyChart(LocalDate from, LocalDate to, String labelFormat) {
        // Fetch actual data
        List<Object[]> rows = paymentRepository.getDailyBreakdown(from, to);
        Map<LocalDate, Double> dayMap = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate d     = (LocalDate) row[0];
            double    total = ((Number) row[1]).doubleValue();
            dayMap.put(d, total);
        }

        List<ChartPoint> chart = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            String label;
            if ("d".equals(labelFormat)) {
                label = String.valueOf(cursor.getDayOfMonth());
            } else {
                // "EEE"
                label = cursor.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            }
            chart.add(new ChartPoint(label, dayMap.getOrDefault(cursor, 0.0)));
            cursor = cursor.plusDays(1);
        }
        return chart;
    }

    private double nvl(Double v) { return v != null ? v : 0.0; }
    private long nvl(Long v)     { return v != null ? v : 0L; }
}
