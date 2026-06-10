package com.powerhouse.fitness.service;

import com.powerhouse.fitness.dto.response.DashboardStats;
import com.powerhouse.fitness.entity.Member.MemberStatus;
import com.powerhouse.fitness.entity.Payment.PaymentStatus;
import com.powerhouse.fitness.repository.MemberRepository;
import com.powerhouse.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public DashboardStats getStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        long total = memberRepository.count();
        long active = memberRepository.findByStatus(MemberStatus.ACTIVE).size();
        long expiring = memberRepository.findByStatus(MemberStatus.EXPIRING).size();
        long expired = memberRepository.findByStatus(MemberStatus.EXPIRED).size();
        long overdue = paymentRepository.findByStatus(PaymentStatus.DUE).size();

        Double collected = paymentRepository.getTotalCollected();
        Double pending = paymentRepository.getTotalPending();

        return DashboardStats.builder()
                .totalMembers(total)
                .activeMembers(active + expiring)
                .expiringThisWeek(expiring)
                .expiredMembers(expired)
                .overduePayments(overdue)
                .totalCollected(collected != null ? collected : 0)
                .totalPending(pending != null ? pending : 0)
                .build();
    }
}