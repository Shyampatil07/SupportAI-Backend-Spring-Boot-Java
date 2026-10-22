package com.supportai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalTickets;

    private long openTickets;

    private long inProgressTickets;

    private long resolvedTickets;

    private long closedTickets;

    private Map<String, Long> statusDistribution;

    private Map<String, Long> priorityDistribution;

    private Map<String, Long> categoryDistribution;

    private List<AgentWorkloadResponse> agentWorkload;
    
    private long unassignedTickets;

    private List<OverloadedAgentResponse> overloadedAgents;
    
    
}