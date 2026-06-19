package com.powerhouse.fitness.service;

import com.powerhouse.fitness.dto.request.MemberRequest;
import com.powerhouse.fitness.dto.response.MemberResponse;
import com.powerhouse.fitness.entity.Attendance;
import com.powerhouse.fitness.entity.Member;
import com.powerhouse.fitness.entity.Member.MemberStatus;
import com.powerhouse.fitness.entity.Payment;
import com.powerhouse.fitness.entity.Plan;
import com.powerhouse.fitness.repository.AttendanceRepository;
import com.powerhouse.fitness.repository.MemberRepository;
import com.powerhouse.fitness.repository.PaymentRepository;
import com.powerhouse.fitness.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByStatus(String status) {
        MemberStatus memberStatus = MemberStatus.valueOf(status.toUpperCase());
        return memberRepository.findByStatus(memberStatus)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(MemberRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanId()));

        LocalDate endDate = request.getPlanStartDate().plusDays(plan.getDurationDays());

        Member member = Member.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .joinDate(LocalDate.now())
                .assignedTrainer(request.getAssignedTrainer())
                .notes(request.getNotes())
                .plan(plan)
                .planStartDate(request.getPlanStartDate())
                .planEndDate(endDate)
                .status(resolveStatus(endDate))
                .totalVisitsThisMonth(0)
                .build();

        Member saved = memberRepository.save(member);

        // Record initial payment — record for both PAID and DUE
        boolean isDue = "DUE".equalsIgnoreCase(request.getPaymentStatus());
        if (request.getPaymentAmount() > 0 || isDue) {
            double amount = request.getPaymentAmount() > 0
                    ? request.getPaymentAmount() : plan.getPrice();
            Payment payment = Payment.builder()
                    .member(saved)
                    .amount(amount)
                    .date(LocalDate.now())
                    .planName(plan.getName())
                    .status(isDue ? Payment.PaymentStatus.DUE : Payment.PaymentStatus.PAID)
                    .mode(!isDue && request.getPaymentMode() != null
                            ? Payment.PaymentMode.valueOf(request.getPaymentMode().toUpperCase())
                            : (!isDue ? Payment.PaymentMode.CASH : null))
                    .build();
            paymentRepository.save(payment);
        }

        return toResponse(saved);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        LocalDate endDate = request.getPlanStartDate().plusDays(plan.getDurationDays());

        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());
        member.setAddress(request.getAddress());
        member.setDateOfBirth(request.getDateOfBirth());
        member.setAssignedTrainer(request.getAssignedTrainer());
        member.setNotes(request.getNotes());
        member.setPlan(plan);
        member.setPlanStartDate(request.getPlanStartDate());
        member.setPlanEndDate(endDate);
        member.setStatus(resolveStatus(endDate));

        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse renewPlan(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        LocalDate startDate = request.getPlanStartDate() != null
                ? request.getPlanStartDate() : LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());

        member.setPlan(plan);
        member.setPlanStartDate(startDate);
        member.setPlanEndDate(endDate);
        member.setStatus(MemberStatus.ACTIVE);

        memberRepository.save(member);

        // Record renewal payment — record for both PAID and DUE
        boolean isDue = "DUE".equalsIgnoreCase(request.getPaymentStatus());
        if (request.getPaymentAmount() > 0 || isDue) {
            double amount = request.getPaymentAmount() > 0
                    ? request.getPaymentAmount() : plan.getPrice();
            Payment payment = Payment.builder()
                    .member(member)
                    .amount(amount)
                    .date(LocalDate.now())
                    .planName(plan.getName())
                    .status(isDue ? Payment.PaymentStatus.DUE : Payment.PaymentStatus.PAID)
                    .mode(!isDue && request.getPaymentMode() != null
                            ? Payment.PaymentMode.valueOf(request.getPaymentMode().toUpperCase())
                            : (!isDue ? Payment.PaymentMode.CASH : null))
                    .build();
            paymentRepository.save(payment);
        }

        return toResponse(member);
    }

    @Transactional
    public MemberResponse checkIn(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));

        LocalDate today = LocalDate.now();

        if (!attendanceRepository.existsByMemberIdAndDate(id, today)) {
            Attendance attendance = Attendance.builder()
                    .member(member)
                    .date(today)
                    .visited(true)
                    .build();
            attendanceRepository.save(attendance);
            member.setLastCheckIn(today);
            member.setTotalVisitsThisMonth(member.getTotalVisitsThisMonth() + 1);
            memberRepository.save(member);
        }

        return toResponse(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // Refresh all member statuses — call from scheduler
    @Transactional
    public void refreshAllStatuses() {
        List<Member> all = memberRepository.findAll();
        for (Member m : all) {
            if (m.getStatus() != MemberStatus.PAUSED) {
                m.setStatus(resolveStatus(m.getPlanEndDate()));
            }
        }
        memberRepository.saveAll(all);
    }

    @Transactional
    public MemberResponse patchStatus(Long id, String status) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found: " + id));
        try {
            member.setStatus(MemberStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
        return toResponse(memberRepository.save(member));
    }


    private MemberStatus resolveStatus(LocalDate endDate) {
        if (endDate == null) return MemberStatus.ACTIVE;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (daysLeft < 0) return MemberStatus.EXPIRED;
        if (daysLeft <= 7) return MemberStatus.EXPIRING;
        return MemberStatus.ACTIVE;
    }

    private MemberResponse toResponse(Member m) {
        long daysRemaining = m.getPlanEndDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), m.getPlanEndDate()) : 0;
        return MemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .phone(m.getPhone())
                .email(m.getEmail())
                .address(m.getAddress())
                .dateOfBirth(m.getDateOfBirth())
                .joinDate(m.getJoinDate())
                .assignedTrainer(m.getAssignedTrainer())
                .notes(m.getNotes())
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .planName(m.getPlan() != null ? m.getPlan().getName() : null)
                .planType(m.getPlan() != null ? m.getPlan().getType().name() : null)
                .planStartDate(m.getPlanStartDate())
                .planEndDate(m.getPlanEndDate())
                .lastCheckIn(m.getLastCheckIn())
                .totalVisitsThisMonth(m.getTotalVisitsThisMonth())
                .daysRemaining((int) daysRemaining)
                .build();
    }
}