package com.powerhouse.fitness.repository;

import com.powerhouse.fitness.entity.Payment;
import com.powerhouse.fitness.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMemberId(Long memberId);
    List<Payment> findByStatus(PaymentStatus status);

    // ── Overall ───────────────────────────────────────────────────────────────

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID'")
    Double getTotalCollected();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'DUE'")
    Double getTotalPending();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID'")
    Long getTotalPaidCount();

    // ── Daily ─────────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.date = :date")
    double getDailyCollected(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'DUE' AND p.date = :date")
    double getDailyPending(@Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID' AND p.date = :date")
    long getDailyCount(@Param("date") LocalDate date);

    // ── Range (used for weekly & monthly breakdowns) ──────────────────────────

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.date BETWEEN :from AND :to")
    double getRangeCollected(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'DUE' AND p.date BETWEEN :from AND :to")
    double getRangePending(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PAID' AND p.date BETWEEN :from AND :to")
    long getRangeCount(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Daily breakdown between two dates.
     * Returns Object[]{date (LocalDate), sum (Double)}
     */
    @Query("SELECT p.date, COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.date BETWEEN :from AND :to " +
           "GROUP BY p.date ORDER BY p.date")
    List<Object[]> getDailyBreakdown(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ── Monthly ───────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.date) = :year AND MONTH(p.date) = :month")
    double getMonthlyCollected(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'DUE' AND YEAR(p.date) = :year AND MONTH(p.date) = :month")
    double getMonthlyPending(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.date) = :year AND MONTH(p.date) = :month")
    long getMonthlyCount(@Param("year") int year, @Param("month") int month);

    // ── Yearly ────────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.date) = :year")
    double getYearlyCollected(@Param("year") int year);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'DUE' AND YEAR(p.date) = :year")
    double getYearlyPending(@Param("year") int year);

    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.date) = :year")
    long getYearlyCount(@Param("year") int year);

    /**
     * Monthly breakdown for a given year.
     * Returns Object[]{month (Integer 1-12), sum (Double)}
     */
    @Query("SELECT MONTH(p.date), COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.date) = :year " +
           "GROUP BY MONTH(p.date) ORDER BY MONTH(p.date)")
    List<Object[]> getMonthlyBreakdown(@Param("year") int year);

    /**
     * Yearly breakdown (all years in DB).
     * Returns Object[]{year (Integer), sum (Double)}
     */
    @Query("SELECT YEAR(p.date), COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.status = 'PAID' " +
           "GROUP BY YEAR(p.date) ORDER BY YEAR(p.date)")
    List<Object[]> getYearlyBreakdown();
}