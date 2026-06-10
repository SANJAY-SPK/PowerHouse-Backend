package com.powerhouse.fitness.repository;

import com.powerhouse.fitness.entity.Member;
import com.powerhouse.fitness.entity.Member.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByStatus(MemberStatus status);
    List<Member> findByPlanEndDateBefore(LocalDate date);
    List<Member> findByPlanEndDateBetween(LocalDate from, LocalDate to);
    boolean existsByPhone(String phone);
}