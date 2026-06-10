package com.powerhouse.fitness.controller;

import com.powerhouse.fitness.entity.Plan;
import com.powerhouse.fitness.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<Plan>> getAll() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<Plan> create(@RequestBody Plan plan) {
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plan> update(@PathVariable Long id, @RequestBody Plan plan) {
        return ResponseEntity.ok(planService.updatePlan(id, plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}