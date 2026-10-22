package com.supportai.controller;

import com.supportai.dto.AgentDashboardResponse;
import com.supportai.service.AgentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('AGENT')")
public class AgentDashboardController {

    private final AgentDashboardService agentDashboardService;

    @GetMapping
    public AgentDashboardResponse getDashboard(
            Authentication authentication) {

        return agentDashboardService.getDashboard(
                authentication.getName()
        );
    }
}