package com.powerhouse.fitness.repository;

import com.powerhouse.fitness.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMemberIdAndDateBetween(Long memberId, LocalDate from, LocalDate to);
    boolean existsByMemberIdAndDate(Long memberId, LocalDate date);
}