package com.powerhouse.fitness.service;

import com.powerhouse.fitness.dto.request.PaymentRequest;
import com.powerhouse.fitness.dto.response.PaymentResponse;
import com.powerhouse.fitness.entity.Member;
import com.powerhouse.fitness.entity.Payment;
import com.powerhouse.fitness.repository.MemberRepository;
import com.powerhouse.fitness.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByMember(Long memberId) {
        return paymentRepository.findByMemberId(memberId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getDuePayments() {
        return paymentRepository.findByStatus(Payment.PaymentStatus.DUE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        Payment payment = Payment.builder()
                .member(member)
                .amount(request.getAmount())
                .date(LocalDate.now())
                .planName(request.getPlanName() != null
                        ? request.getPlanName()
                        : (member.getPlan() != null ? member.getPlan().getName() : null))
                .status(request.getStatus() != null && request.getStatus().equalsIgnoreCase("DUE")
                        ? Payment.PaymentStatus.DUE
                        : Payment.PaymentStatus.PAID)
                .mode(request.getMode() != null
                        ? Payment.PaymentMode.valueOf(request.getMode().toUpperCase())
                        : (request.getStatus() != null && request.getStatus().equalsIgnoreCase("DUE")
                            ? null
                            : Payment.PaymentMode.CASH))
                .build();


        return toResponse(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse markAsPaid(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setDate(LocalDate.now());
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .memberId(p.getMember().getId())
                .memberName(p.getMember().getName())
                .amount(p.getAmount())
                .date(p.getDate())
                .planName(p.getPlanName())
                .status(p.getStatus().name())
                .mode(p.getMode() != null ? p.getMode().name() : null)
                .build();
    }
}