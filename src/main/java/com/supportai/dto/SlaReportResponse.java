package com.supportai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class SlaReportResponse {

    private LocalDate from;
    private LocalDate to;

    private long totalTickets;
    private long breachedTickets;
    private long atRiskTickets;
    private long compliantTickets;
    private long escalatedTickets;
    private long criticalBreaches;

    private double averageResolutionTimeHours;
}