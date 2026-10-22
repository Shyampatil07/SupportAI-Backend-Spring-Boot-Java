package com.supportai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentDashboardResponse {

    private long totalAssigned;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long highPriorityTickets;
    private long unreadNotifications;
}