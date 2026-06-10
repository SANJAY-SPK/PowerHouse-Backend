package com.powerhouse.fitness.service;

import com.powerhouse.fitness.entity.Plan;
import com.powerhouse.fitness.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public List<Plan> getAllPlans() {
        return planRepository.findByActiveTrue();
    }

    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + id));
    }

    public Plan createPlan(Plan plan) {
        plan.setActive(true);
        return planRepository.save(plan);
    }

    public Plan updatePlan(Long id, Plan updated) {
        Plan plan = getPlanById(id);
        plan.setName(updated.getName());
        plan.setType(updated.getType());
        plan.setDurationDays(updated.getDurationDays());
        plan.setPrice(updated.getPrice());
        plan.setFeatures(updated.getFeatures());
        return planRepository.save(plan);
    }

    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        plan.setActive(false); // soft delete
        planRepository.save(plan);
    }
}