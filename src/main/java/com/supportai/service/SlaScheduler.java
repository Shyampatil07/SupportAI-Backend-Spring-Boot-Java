package com.supportai.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final SlaBreachService slaBreachService;

    @Scheduled(fixedDelay = 60000)
    public void checkSlaBreaches() {

        slaBreachService.detectBreaches();
    }
}