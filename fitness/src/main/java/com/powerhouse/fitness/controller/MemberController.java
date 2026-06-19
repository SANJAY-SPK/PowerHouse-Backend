package com.powerhouse.fitness.controller;

import com.powerhouse.fitness.dto.request.MemberRequest;
import com.powerhouse.fitness.dto.response.MemberResponse;
import com.powerhouse.fitness.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAll(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(memberService.getMembersByStatus(status));
        }
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.addMember(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<MemberResponse> renew(@PathVariable Long id,
                                                @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.renewPlan(id, request));
    }

    /** Lightweight status patch — only changes the status field.
     *  Body: { "status": "PAUSED" | "ACTIVE" | "EXPIRED" }
     *  e.g. PATCH /api/members/5/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<MemberResponse> patchStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(memberService.patchStatus(id, status.toUpperCase()));
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<MemberResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.checkIn(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}