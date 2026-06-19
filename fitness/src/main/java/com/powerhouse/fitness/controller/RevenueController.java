package com.powerhouse.fitness.controller;

import com.powerhouse.fitness.dto.response.RevenueStats;
import com.powerhouse.fitness.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/revenue")
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    /** All-time totals + year-by-year chart */
    @GetMapping("/overall")
    public ResponseEntity<RevenueStats> getOverall() {
        return ResponseEntity.ok(revenueService.getOverallRevenue());
    }

    /** Totals for a given year + monthly breakdown chart
     *  e.g. GET /api/revenue/yearly?year=2026 */
    @GetMapping("/yearly")
    public ResponseEntity<RevenueStats> getYearly(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        return ResponseEntity.ok(revenueService.getYearlyRevenue(year));
    }

    /** Totals for a given month + daily breakdown chart
     *  e.g. GET /api/revenue/monthly?year=2026&month=6 */
    @GetMapping("/monthly")
    public ResponseEntity<RevenueStats> getMonthly(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        return ResponseEntity.ok(revenueService.getMonthlyRevenue(year, month));
    }

    /** Totals for the Mon–Sun week containing date + daily breakdown chart
     *  e.g. GET /api/revenue/weekly?date=2026-06-15 */
    @GetMapping("/weekly")
    public ResponseEntity<RevenueStats> getWeekly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(revenueService.getWeeklyRevenue(date != null ? date : LocalDate.now()));
    }

    /** Totals for a single day
     *  e.g. GET /api/revenue/daily?date=2026-06-15 */
    @GetMapping("/daily")
    public ResponseEntity<RevenueStats> getDaily(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(revenueService.getDailyRevenue(date != null ? date : LocalDate.now()));
    }
}
