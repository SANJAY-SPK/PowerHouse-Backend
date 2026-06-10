package com.powerhouse.fitness.scheduler;

import com.powerhouse.fitness.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusScheduler {

    private final MemberService memberService;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshMemberStatuses() {
        log.info("Running scheduled member status refresh...");
        memberService.refreshAllStatuses();
        log.info("Member status refresh complete.");
    }
}