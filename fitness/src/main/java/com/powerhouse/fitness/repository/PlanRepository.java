package com.powerhouse.fitness.repository;

import com.powerhouse.fitness.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrue();
}