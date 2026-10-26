package com.supportai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SlaAnalyticsResponse {

    private long totalActiveTickets;
    private long slaBreachedTickets;
    private long slaAtRiskTickets;
    private long slaCompliantTickets;
    private long escalatedTickets;
    private long criticalBreaches;
    private double averageResolutionTimeHours;
}