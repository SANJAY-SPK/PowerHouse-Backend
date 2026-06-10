package com.powerhouse.fitness.repository;

import com.powerhouse.fitness.entity.Payment;
import com.powerhouse.fitness.entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberId(Long memberId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID'")
    Double getTotalCollected();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'DUE'")
    Double getTotalPending();
}