package com.supportai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentWorkloadResponse {

    private Long agentId;

    private String agentName;

    private long assignedTickets;

    private long openTickets;

    private long inProgressTickets;

    private long resolvedTickets;

    private long closedTickets;
}